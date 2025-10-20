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
@Import(ProductShoppingCartService.class)
class ProductShoppingCartServiceTest {

    @Autowired
    private ProductShoppingCartService productShoppingCartService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private ProductEntity product;
    private List<UserEntity> userList = new ArrayList<>();
    private List<ShoppingCartEntity> shoppingCartList = new ArrayList<>();

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
        product = factory.manufacturePojo(ProductEntity.class);
        product.setPrice(100.0);
        product.setStock(10);
        entityManager.persist(product);

        for (int i = 0; i < 3; i++) {
            UserEntity user = factory.manufacturePojo(UserEntity.class);
            entityManager.persist(user);
            userList.add(user);

            ShoppingCartEntity entity = factory.manufacturePojo(ShoppingCartEntity.class);
            entity.setUser(user);
            entity.setTotal(100.0 * (i + 1));
            entityManager.persist(entity);
            shoppingCartList.add(entity);

            entity.getProducts().add(product);
            product.getShoppingCarts().add(entity);
        }

        entityManager.flush();
    }

    @Test
    void testAddShoppingCart() throws EntityNotFoundException {
        ProductEntity newProduct = factory.manufacturePojo(ProductEntity.class);
        newProduct.setPrice(150.0);
        newProduct.setStock(5);
        entityManager.persist(newProduct);

        UserEntity newUser = factory.manufacturePojo(UserEntity.class);
        entityManager.persist(newUser);

        ShoppingCartEntity cart = factory.manufacturePojo(ShoppingCartEntity.class);
        cart.setUser(newUser);
        cart.setTotal(0.0);
        entityManager.persist(cart);

        ShoppingCartEntity result = productShoppingCartService.addShoppingCart(newProduct.getId(), cart.getId());

        assertNotNull(result);
        assertEquals(cart.getId(), result.getId());
    }

    @Test
    void testAddShoppingCartInvalidProduct() {
        UserEntity user = factory.manufacturePojo(UserEntity.class);
        entityManager.persist(user);

        ShoppingCartEntity cart = factory.manufacturePojo(ShoppingCartEntity.class);
        cart.setUser(user);
        entityManager.persist(cart);

        Long invalidProductId = 0L;
        Long validCartId = cart.getId();

        assertThrows(EntityNotFoundException.class,
                () -> productShoppingCartService.addShoppingCart(invalidProductId, validCartId));
    }

    @Test
    void testAddInvalidShoppingCart() {
        Long invalidCartId = 0L;
        Long validProductId = product.getId();

        assertThrows(EntityNotFoundException.class,
                () -> productShoppingCartService.addShoppingCart(validProductId, invalidCartId));
    }

    @Test
    void testGetShoppingCarts() throws EntityNotFoundException {
        List<ShoppingCartEntity> carts = productShoppingCartService.getShoppingCarts(product.getId());

        assertEquals(shoppingCartList.size(), carts.size());
        for (ShoppingCartEntity cart : shoppingCartList) {
            assertTrue(carts.contains(cart));
        }
    }

    @Test
    void testGetShoppingCartsInvalidProduct() {
        Long invalidProductId = 0L;

        assertThrows(EntityNotFoundException.class,
                () -> productShoppingCartService.getShoppingCarts(invalidProductId));
    }

    @Test
    void testGetShoppingCart() throws EntityNotFoundException, IllegalOperationException {
        ShoppingCartEntity cart = shoppingCartList.get(0);
        Long productId = product.getId();
        Long cartId = cart.getId();

        ShoppingCartEntity result = productShoppingCartService.getShoppingCart(productId, cartId);

        assertNotNull(result);
        assertEquals(cartId, result.getId());
        assertEquals(cart.getTotal(), result.getTotal());
    }

    @Test
    void testGetShoppingCartInvalidProduct() {
        ShoppingCartEntity cart = shoppingCartList.get(0);
        Long invalidProductId = 0L;
        Long cartId = cart.getId();

        assertThrows(EntityNotFoundException.class,
                () -> productShoppingCartService.getShoppingCart(invalidProductId, cartId));
    }

    @Test
    void testGetInvalidShoppingCart() {
        Long invalidCartId = 0L;
        Long validProductId = product.getId();

        assertThrows(EntityNotFoundException.class,
                () -> productShoppingCartService.getShoppingCart(validProductId, invalidCartId));
    }

    @Test
    void testGetShoppingCartNotAssociatedProduct() {
        ProductEntity newProduct = factory.manufacturePojo(ProductEntity.class);
        newProduct.setStock(5);
        entityManager.persist(newProduct);

        UserEntity newUser = factory.manufacturePojo(UserEntity.class);
        entityManager.persist(newUser);

        ShoppingCartEntity newCart = factory.manufacturePojo(ShoppingCartEntity.class);
        newCart.setUser(newUser);
        entityManager.persist(newCart);

        Long newProductId = newProduct.getId();
        Long newCartId = newCart.getId();

        assertThrows(IllegalOperationException.class,
                () -> productShoppingCartService.getShoppingCart(newProductId, newCartId));
    }

    @Test
    void testReplaceShoppingCarts() throws EntityNotFoundException {
        List<ShoppingCartEntity> newList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            UserEntity newUser = factory.manufacturePojo(UserEntity.class);
            entityManager.persist(newUser);

            ShoppingCartEntity entity = factory.manufacturePojo(ShoppingCartEntity.class);
            entity.setUser(newUser);
            entity.setTotal(50.0 * (i + 1));
            entityManager.persist(entity);
            newList.add(entity);
        }

        List<ShoppingCartEntity> result = productShoppingCartService.replaceShoppingCarts(product.getId(), newList);

        assertEquals(newList.size(), result.size());
        for (ShoppingCartEntity cart : newList) {
            assertTrue(result.contains(cart));
        }
    }

    @Test
    void testReplaceShoppingCartsInvalidProduct() {
        UserEntity newUser = factory.manufacturePojo(UserEntity.class);
        entityManager.persist(newUser);

        ShoppingCartEntity entity = factory.manufacturePojo(ShoppingCartEntity.class);
        entity.setUser(newUser);
        entityManager.persist(entity);

        List<ShoppingCartEntity> newList = new ArrayList<>();
        newList.add(entity);

        Long invalidProductId = 0L;

        assertThrows(EntityNotFoundException.class,
                () -> productShoppingCartService.replaceShoppingCarts(invalidProductId, newList));
    }

    @Test
    void testReplaceInvalidShoppingCarts() {
        ShoppingCartEntity entity = factory.manufacturePojo(ShoppingCartEntity.class);
        entity.setId(0L);

        List<ShoppingCartEntity> newList = new ArrayList<>();
        newList.add(entity);

        Long validProductId = product.getId();

        assertThrows(EntityNotFoundException.class,
                () -> productShoppingCartService.replaceShoppingCarts(validProductId, newList));
    }

    @Test
    void testRemoveShoppingCart() throws EntityNotFoundException {
        ShoppingCartEntity cart = shoppingCartList.get(0);
        Long productId = product.getId();
        Long cartId = cart.getId();

        productShoppingCartService.removeShoppingCart(productId, cartId);

        entityManager.flush();
        entityManager.clear();

        ProductEntity updatedProduct = entityManager.find(ProductEntity.class, productId);
        assertFalse(updatedProduct.getShoppingCarts().contains(cart));
    }

    @Test
    void testRemoveAllShoppingCarts() throws EntityNotFoundException {
        Long productId = product.getId();

        for (ShoppingCartEntity cart : shoppingCartList) {
            productShoppingCartService.removeShoppingCart(productId, cart.getId());
        }

        List<ShoppingCartEntity> remainingCarts = productShoppingCartService.getShoppingCarts(productId);
        assertTrue(remainingCarts.isEmpty());
    }

    @Test
    void testRemoveShoppingCartInvalidProduct() {
        ShoppingCartEntity cart = shoppingCartList.get(0);
        Long invalidProductId = 0L;
        Long cartId = cart.getId();

        assertThrows(EntityNotFoundException.class,
                () -> productShoppingCartService.removeShoppingCart(invalidProductId, cartId));
    }

    @Test
    void testRemoveInvalidShoppingCart() {
        Long productId = product.getId();
        Long invalidCartId = 0L;

        assertThrows(EntityNotFoundException.class,
                () -> productShoppingCartService.removeShoppingCart(productId, invalidCartId));
    }
}
