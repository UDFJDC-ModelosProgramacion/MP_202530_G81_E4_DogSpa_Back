package co.edu.udistrital.mdp.back.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.entities.ShoppingCartEntity;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import(ShoppingCartProductService.class)
class ShoppingCartProductServiceTest {

    @Autowired
    private ShoppingCartProductService shoppingCartProductService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private ShoppingCartEntity shoppingCart;
    private UserEntity user;
    private List<ProductEntity> productList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from ShoppingCartEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from ProductEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from UserEntity").executeUpdate();
    }

    private void insertData() {
        user = factory.manufacturePojo(UserEntity.class);
        entityManager.persist(user);

        shoppingCart = factory.manufacturePojo(ShoppingCartEntity.class);
        shoppingCart.setUser(user);
        shoppingCart.setTotal(0.0);
        entityManager.persist(shoppingCart);

        for (int i = 0; i < 3; i++) {
            ProductEntity entity = factory.manufacturePojo(ProductEntity.class);
            entity.setPrice(100.0 + i * 10);
            entity.setStock(10 + i);
            entityManager.persist(entity);
            productList.add(entity);
            entity.getShoppingCarts().add(shoppingCart);
            shoppingCart.getProducts().add(entity);
        }
        shoppingCart.setTotal(330.0);
        entityManager.flush();
    }

    @Test
    void testAddProduct() throws EntityNotFoundException, IllegalOperationException {
        ProductEntity newProduct = factory.manufacturePojo(ProductEntity.class);
        newProduct.setPrice(150.0);
        newProduct.setStock(5);
        entityManager.persist(newProduct);

        ProductEntity result = shoppingCartProductService.addProduct(shoppingCart.getId(), newProduct.getId());
        assertNotNull(result);
        assertEquals(newProduct.getId(), result.getId());
        assertEquals(newProduct.getName(), result.getName());
        assertEquals(150.0, result.getPrice());

        entityManager.flush();
        entityManager.clear();
        ShoppingCartEntity updatedCart = entityManager.find(ShoppingCartEntity.class, shoppingCart.getId());
        assertEquals(480.0, updatedCart.getTotal());
    }

    @Test
    void testAddProductInvalidShoppingCart() {
        assertThrows(EntityNotFoundException.class, () -> {
            ProductEntity newProduct = factory.manufacturePojo(ProductEntity.class);
            newProduct.setStock(5);
            entityManager.persist(newProduct);
            shoppingCartProductService.addProduct(0L, newProduct.getId());
        });
    }

    @Test
    void testAddInvalidProduct() {
        assertThrows(EntityNotFoundException.class, () -> {
            shoppingCartProductService.addProduct(shoppingCart.getId(), 0L);
        });
    }

    @Test
    void testAddProductWithoutStock() {
        assertThrows(IllegalOperationException.class, () -> {
            ProductEntity newProduct = factory.manufacturePojo(ProductEntity.class);
            newProduct.setPrice(100.0);
            newProduct.setStock(0);
            entityManager.persist(newProduct);
            shoppingCartProductService.addProduct(shoppingCart.getId(), newProduct.getId());
        });
    }

    @Test
    void testAddProductAlreadyInCart() throws EntityNotFoundException, IllegalOperationException {
        ProductEntity existingProduct = productList.get(0);
        double totalBefore = shoppingCart.getTotal();
        
        ProductEntity result = shoppingCartProductService.addProduct(shoppingCart.getId(), existingProduct.getId());
        
        assertNotNull(result);
        entityManager.flush();
        entityManager.clear();
        ShoppingCartEntity updatedCart = entityManager.find(ShoppingCartEntity.class, shoppingCart.getId());
        assertEquals(totalBefore, updatedCart.getTotal());
    }

    @Test
    void testGetProducts() throws EntityNotFoundException {
        List<ProductEntity> products = shoppingCartProductService.getProducts(shoppingCart.getId());

        assertEquals(productList.size(), products.size());
        for (ProductEntity product : productList) {
            assertTrue(products.contains(product));
        }
    }

    @Test
    void testGetProductsInvalidShoppingCart() {
        assertThrows(EntityNotFoundException.class, () -> {
            shoppingCartProductService.getProducts(0L);
        });
    }

    @Test
    void testGetProduct() throws EntityNotFoundException, IllegalOperationException {
        ProductEntity product = productList.get(0);
        ProductEntity result = shoppingCartProductService.getProduct(shoppingCart.getId(), product.getId());
        
        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getPrice(), result.getPrice());
    }

    @Test
    void testGetProductInvalidShoppingCart() {
        assertThrows(EntityNotFoundException.class, () -> {
            ProductEntity product = productList.get(0);
            shoppingCartProductService.getProduct(0L, product.getId());
        });
    }

    @Test
    void testGetInvalidProduct() {
        assertThrows(EntityNotFoundException.class, () -> {
            shoppingCartProductService.getProduct(shoppingCart.getId(), 0L);
        });
    }

    @Test
    void testGetProductNotAssociatedShoppingCart() {
        assertThrows(IllegalOperationException.class, () -> {
            UserEntity newUser = factory.manufacturePojo(UserEntity.class);
            entityManager.persist(newUser);

            ShoppingCartEntity newCart = factory.manufacturePojo(ShoppingCartEntity.class);
            newCart.setUser(newUser);
            entityManager.persist(newCart);

            ProductEntity newProduct = factory.manufacturePojo(ProductEntity.class);
            newProduct.setStock(5);
            entityManager.persist(newProduct);

            shoppingCartProductService.getProduct(newCart.getId(), newProduct.getId());
        });
    }

    @Test
    void testReplaceProducts() throws EntityNotFoundException {
        List<ProductEntity> newList = new ArrayList<>();
        double expectedTotal = 0.0;
        
        for (int i = 0; i < 2; i++) {
            ProductEntity entity = factory.manufacturePojo(ProductEntity.class);
            entity.setPrice(50.0 + i * 10);
            entity.setStock(10);
            entityManager.persist(entity);
            newList.add(entity);
            expectedTotal += entity.getPrice();
        }
        
        List<ProductEntity> result = shoppingCartProductService.replaceProducts(shoppingCart.getId(), newList);
        
        assertEquals(newList.size(), result.size());
        for (ProductEntity product : newList) {
            assertTrue(result.contains(product));
        }
        
        entityManager.flush();
        entityManager.clear();
        ShoppingCartEntity updatedCart = entityManager.find(ShoppingCartEntity.class, shoppingCart.getId());
        assertEquals(expectedTotal, updatedCart.getTotal());
    }

    @Test
    void testReplaceProductsInvalidShoppingCart() {
        assertThrows(EntityNotFoundException.class, () -> {
            List<ProductEntity> newList = new ArrayList<>();
            ProductEntity entity = factory.manufacturePojo(ProductEntity.class);
            entity.setStock(5);
            entityManager.persist(entity);
            newList.add(entity);
            shoppingCartProductService.replaceProducts(0L, newList);
        });
    }

    @Test
    void testReplaceInvalidProducts() {
        assertThrows(EntityNotFoundException.class, () -> {
            List<ProductEntity> newList = new ArrayList<>();
            ProductEntity entity = factory.manufacturePojo(ProductEntity.class);
            entity.setId(0L);
            newList.add(entity);
            shoppingCartProductService.replaceProducts(shoppingCart.getId(), newList);
        });
    }

    @Test
    void testRemoveProduct() throws EntityNotFoundException {
        ProductEntity product = productList.get(0);
        double priceBefore = product.getPrice();
        double totalBefore = shoppingCart.getTotal();
        
        shoppingCartProductService.removeProduct(shoppingCart.getId(), product.getId());
        
        entityManager.flush();
        entityManager.clear();
        ShoppingCartEntity updatedCart = entityManager.find(ShoppingCartEntity.class, shoppingCart.getId());
        assertEquals(totalBefore - priceBefore, updatedCart.getTotal());
        assertFalse(updatedCart.getProducts().contains(product));
    }

    @Test
    void testRemoveAllProducts() throws EntityNotFoundException {
        for (ProductEntity product : productList) {
            shoppingCartProductService.removeProduct(shoppingCart.getId(), product.getId());
        }
        
        List<ProductEntity> remainingProducts = shoppingCartProductService.getProducts(shoppingCart.getId());
        assertTrue(remainingProducts.isEmpty());
        
        entityManager.flush();
        entityManager.clear();
        ShoppingCartEntity updatedCart = entityManager.find(ShoppingCartEntity.class, shoppingCart.getId());
        assertEquals(0.0, updatedCart.getTotal());
    }

    @Test
    void testRemoveProductInvalidShoppingCart() {
        assertThrows(EntityNotFoundException.class, () -> {
            ProductEntity product = productList.get(0);
            shoppingCartProductService.removeProduct(0L, product.getId());
        });
    }

    @Test
    void testRemoveInvalidProduct() {
        assertThrows(EntityNotFoundException.class, () -> {
            shoppingCartProductService.removeProduct(shoppingCart.getId(), 0L);
        });
    }
}