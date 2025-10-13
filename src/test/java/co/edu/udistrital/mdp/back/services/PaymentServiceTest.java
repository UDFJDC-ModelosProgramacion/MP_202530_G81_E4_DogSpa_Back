package co.edu.udistrital.mdp.back.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import co.edu.udistrital.mdp.back.entities.PaymentEntity;
import co.edu.udistrital.mdp.back.entities.ShoppingCartEntity;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import(PaymentService.class)
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private List<PaymentEntity> paymentList = new ArrayList<>();
    private ShoppingCartEntity shoppingCart;

    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from PaymentEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from ShoppingCartEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from UserEntity").executeUpdate();
    }

    private void insertData() {
        UserEntity user = factory.manufacturePojo(UserEntity.class);
        entityManager.persist(user);

        shoppingCart = factory.manufacturePojo(ShoppingCartEntity.class);
        shoppingCart.setUser(user);
        entityManager.persist(shoppingCart);

        for (int i = 0; i < 3; i++) {
            PaymentEntity entity = factory.manufacturePojo(PaymentEntity.class);
            entity.setMethod("credit_card");
            entity.setAmount(100.0 + i * 50);
            entity.setStatus("pending");
            entity.setDate(LocalDateTime.now());
            entity.setShoppingCart(shoppingCart);
            entityManager.persist(entity);
            paymentList.add(entity);
        }
    }

    @Test
    void testCreatePayment() throws EntityNotFoundException, IllegalOperationException {
    PaymentEntity newEntity = new PaymentEntity();
    newEntity.setMethod("pse");
    newEntity.setAmount(250.0);
    newEntity.setDate(LocalDateTime.now());
    newEntity.setShoppingCart(shoppingCart);

    PaymentEntity result = paymentService.createPayment(newEntity);
    assertNotNull(result);

    PaymentEntity entity = entityManager.find(PaymentEntity.class, result.getId());
    assertEquals("pse", entity.getMethod());
    assertEquals(250.0, entity.getAmount());
    assertEquals("pending", entity.getStatus());
}

    @Test
    void testCreatePaymentWithoutMethod() {
        assertThrows(IllegalOperationException.class, () -> {
            PaymentEntity newEntity = factory.manufacturePojo(PaymentEntity.class);
            newEntity.setMethod(null);
            newEntity.setAmount(100.0);
            newEntity.setShoppingCart(shoppingCart);
            paymentService.createPayment(newEntity);
        });
    }

    @Test
    void testCreatePaymentWithInvalidMethod() {
        assertThrows(IllegalOperationException.class, () -> {
            PaymentEntity newEntity = factory.manufacturePojo(PaymentEntity.class);
            newEntity.setMethod("bitcoin");
            newEntity.setAmount(100.0);
            newEntity.setShoppingCart(shoppingCart);
            paymentService.createPayment(newEntity);
        });
    }

    @Test
    void testCreatePaymentWithoutCart() {
        assertThrows(IllegalOperationException.class, () -> {
            PaymentEntity newEntity = factory.manufacturePojo(PaymentEntity.class);
            newEntity.setMethod("cash");
            newEntity.setAmount(100.0);
            newEntity.setShoppingCart(null);
            paymentService.createPayment(newEntity);
        });
    }

    @Test
    void testCreatePaymentWithInvalidCart() {
        assertThrows(EntityNotFoundException.class, () -> {
            PaymentEntity newEntity = factory.manufacturePojo(PaymentEntity.class);
            newEntity.setMethod("cash");
            newEntity.setAmount(100.0);
            ShoppingCartEntity invalidCart = new ShoppingCartEntity();
            invalidCart.setId(0L);
            newEntity.setShoppingCart(invalidCart);
            paymentService.createPayment(newEntity);
        });
    }

    @Test
    void testCreatePaymentWithInvalidAmount() {
        assertThrows(IllegalOperationException.class, () -> {
            PaymentEntity newEntity = factory.manufacturePojo(PaymentEntity.class);
            newEntity.setMethod("cash");
            newEntity.setAmount(0.0);
            newEntity.setShoppingCart(shoppingCart);
            paymentService.createPayment(newEntity);
        });
    }

    @Test
    void testGetPayments() {
        List<PaymentEntity> list = paymentService.getPayments();
        assertEquals(paymentList.size(), list.size());
    }

    @Test
    void testGetPayment() throws EntityNotFoundException {
        PaymentEntity entity = paymentList.get(0);
        PaymentEntity result = paymentService.getPayment(entity.getId());
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
    }

    @Test
    void testGetInvalidPayment() {
        assertThrows(EntityNotFoundException.class, () -> {
            paymentService.getPayment(0L);
        });
    }

    @Test
    void testUpdatePayment() throws EntityNotFoundException, IllegalOperationException {
        PaymentEntity entity = paymentList.get(0);
        PaymentEntity updateEntity = new PaymentEntity();
        updateEntity.setMethod("debit_card");
        updateEntity.setAmount(300.0);

        PaymentEntity result = paymentService.updatePayment(entity.getId(), updateEntity);
        
        assertEquals("debit_card", result.getMethod());
        assertEquals(300.0, result.getAmount());
    }

    @Test
    void testUpdateCompletedPayment() {
        assertThrows(IllegalOperationException.class, () -> {
            PaymentEntity entity = paymentList.get(0);
            entity.setStatus("completed");
            entityManager.persist(entity);

            PaymentEntity updateEntity = new PaymentEntity();
            updateEntity.setAmount(500.0);
            paymentService.updatePayment(entity.getId(), updateEntity);
        });
    }

    @Test
    void testUpdateInvalidPayment() {
        assertThrows(EntityNotFoundException.class, () -> {
            PaymentEntity updateEntity = new PaymentEntity();
            updateEntity.setAmount(100.0);
            paymentService.updatePayment(0L, updateEntity);
        });
    }

    @Test
    void testDeletePayment() throws EntityNotFoundException, IllegalOperationException {
        PaymentEntity entity = paymentList.get(0);
        paymentService.deletePayment(entity.getId());
        
        PaymentEntity deleted = entityManager.find(PaymentEntity.class, entity.getId());
        assertNull(deleted);
    }

    @Test
    void testDeleteCompletedPayment() {
        assertThrows(IllegalOperationException.class, () -> {
            PaymentEntity entity = paymentList.get(0);
            entity.setStatus("completed");
            entityManager.persist(entity);
            
            paymentService.deletePayment(entity.getId());
        });
    }

    @Test
    void testDeleteInvalidPayment() {
        assertThrows(EntityNotFoundException.class, () -> {
            paymentService.deletePayment(0L);
        });
    }

    @Test
    void testProcessPayment() throws EntityNotFoundException, IllegalOperationException {
        PaymentEntity entity = paymentList.get(0);
        PaymentEntity result = paymentService.processPayment(entity.getId());
        
        assertEquals("processing", result.getStatus());
    }

    @Test
    void testProcessNonPendingPayment() {
        assertThrows(IllegalOperationException.class, () -> {
            PaymentEntity entity = paymentList.get(0);
            entity.setStatus("completed");
            entityManager.persist(entity);
            
            paymentService.processPayment(entity.getId());
        });
    }

    @Test
    void testCompletePayment() throws EntityNotFoundException, IllegalOperationException {
        PaymentEntity entity = paymentList.get(0);
        entity.setStatus("processing");
        entityManager.persist(entity);
        
        PaymentEntity result = paymentService.completePayment(entity.getId());
        
        assertEquals("completed", result.getStatus());
    }

    @Test
    void testCompleteNonProcessingPayment() {
        assertThrows(IllegalOperationException.class, () -> {
            PaymentEntity entity = paymentList.get(0);
            paymentService.completePayment(entity.getId());
        });
    }

    @Test
    void testCancelPayment() throws EntityNotFoundException, IllegalOperationException {
        PaymentEntity entity = paymentList.get(0);
        PaymentEntity result = paymentService.cancelPayment(entity.getId());
        
        assertEquals("cancelled", result.getStatus());
    }

    @Test
    void testCancelCompletedPayment() {
        assertThrows(IllegalOperationException.class, () -> {
            PaymentEntity entity = paymentList.get(0);
            entity.setStatus("completed");
            entityManager.persist(entity);
            
            paymentService.cancelPayment(entity.getId());
        });
    }

    @Test
    void testGetPaymentsByShoppingCart() throws EntityNotFoundException {
    entityManager.flush();
    entityManager.refresh(shoppingCart);
    
    List<PaymentEntity> payments = paymentService.getPaymentsByShoppingCart(shoppingCart.getId());
    assertEquals(3, payments.size());
}

    @Test
    void testGetPaymentsByInvalidShoppingCart() {
        assertThrows(EntityNotFoundException.class, () -> {
            paymentService.getPaymentsByShoppingCart(0L);
        });
    }
}