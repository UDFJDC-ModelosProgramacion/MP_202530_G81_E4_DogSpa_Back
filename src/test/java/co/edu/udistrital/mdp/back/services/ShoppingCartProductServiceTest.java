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
        ProductEntity newProduct = factory.manufacturePojo(ProductEntity.class);
        newProduct.setStock(5);
        entityManager.persist(newProduct);
        Long invalidCartId = 0L;
        Long validProductId = newProduct.getId();

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartProductService.addProduct(invalidCartId, validProductId));
    }

    @Test
    void testAddInvalidProduct() {
        Long invalidProductId = 0L;
        Long validCartId = shoppingCart.getId();

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartProductService.addProduct(validCartId, invalidProductId));
    }

    @Test
    void testAddProductWithoutStock() {
        ProductEntity newProduct = factory.manufacturePojo(ProductEntity.class);
        newProduct.setPrice(100.0);
        newProduct.setStock(0);
        entityManager.persist(newProduct);
        Long validCartId = shoppingCart.getId();
        Long productId = newProduct.getId();

        assertThrows(IllegalOperationException.class,
                () -> shoppingCartProductService.addProduct(validCartId, productId));
    }

    @Test
    void testAddProductAlreadyInCart() throws EntityNotFoundException, IllegalOperationException {
        ProductEntity existingProduct = productList.get(0);
        double totalBefore = shoppingCart.getTotal();
        Long cartId = shoppingCart.getId();
        Long productId = existingProduct.getId();

        ProductEntity result = shoppingCartProductService.addProduct(cartId, productId);

        assertNotNull(result);
        entityManager.flush();
        entityManager.clear();
        ShoppingCartEntity updatedCart = entityManager.find(ShoppingCartEntity.class, cartId);
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
        Long invalidCartId = 0L;
        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartProductService.getProducts(invalidCartId));
    }

    @Test
    void testGetProduct() throws EntityNotFoundException, IllegalOperationException {
        ProductEntity product = productList.get(0);
        Long cartId = shoppingCart.getId();
        Long productId = product.getId();

        ProductEntity result = shoppingCartProductService.getProduct(cartId, productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getPrice(), result.getPrice());
    }

    @Test
    void testGetProductInvalidShoppingCart() {
        ProductEntity product = productList.get(0);
        Long invalidCartId = 0L;
        Long productId = product.getId();

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartProductService.getProduct(invalidCartId, productId));
    }

    @Test
    void testGetInvalidProduct() {
        Long invalidProductId = 0L;
        Long validCartId = shoppingCart.getId();

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartProductService.getProduct(validCartId, invalidProductId));
    }

    @Test
    void testGetProductNotAssociatedShoppingCart() {
        UserEntity newUser = factory.manufacturePojo(UserEntity.class);
        entityManager.persist(newUser);

        ShoppingCartEntity newCart = factory.manufacturePojo(ShoppingCartEntity.class);
        newCart.setUser(newUser);
        entityManager.persist(newCart);

        ProductEntity newProduct = factory.manufacturePojo(ProductEntity.class);
        newProduct.setStock(5);
        entityManager.persist(newProduct);

        Long newCartId = newCart.getId();
        Long newProductId = newProduct.getId();

        assertThrows(IllegalOperationException.class,
                () -> shoppingCartProductService.getProduct(newCartId, newProductId));
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
        List<ProductEntity> newList = new ArrayList<>();
        ProductEntity entity = factory.manufacturePojo(ProductEntity.class);
        entity.setStock(5);
        entityManager.persist(entity);
        newList.add(entity);
        Long invalidCartId = 0L;

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartProductService.replaceProducts(invalidCartId, newList));
    }

    @Test
    void testReplaceInvalidProducts() {
        ProductEntity entity = factory.manufacturePojo(ProductEntity.class);
        entity.setId(0L);
        List<ProductEntity> newList = new ArrayList<>();
        newList.add(entity);
        Long validCartId = shoppingCart.getId();

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartProductService.replaceProducts(validCartId, newList));
    }

    @Test
    void testRemoveProduct() throws EntityNotFoundException {
        ProductEntity product = productList.get(0);
        double priceBefore = product.getPrice();
        double totalBefore = shoppingCart.getTotal();
        Long cartId = shoppingCart.getId();
        Long productId = product.getId();

        shoppingCartProductService.removeProduct(cartId, productId);

        entityManager.flush();
        entityManager.clear();
        ShoppingCartEntity updatedCart = entityManager.find(ShoppingCartEntity.class, cartId);
        assertEquals(totalBefore - priceBefore, updatedCart.getTotal());
        assertFalse(updatedCart.getProducts().contains(product));
    }

    @Test
    void testRemoveAllProducts() throws EntityNotFoundException {
        Long cartId = shoppingCart.getId();
        for (ProductEntity product : productList) {
            shoppingCartProductService.removeProduct(cartId, product.getId());
        }

        List<ProductEntity> remainingProducts = shoppingCartProductService.getProducts(cartId);
        assertTrue(remainingProducts.isEmpty());

        entityManager.flush();
        entityManager.clear();
        ShoppingCartEntity updatedCart = entityManager.find(ShoppingCartEntity.class, cartId);
        assertEquals(0.0, updatedCart.getTotal());
    }

    @Test
    void testRemoveProductInvalidShoppingCart() {
        ProductEntity product = productList.get(0);
        Long invalidCartId = 0L;
        Long productId = product.getId();

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartProductService.removeProduct(invalidCartId, productId));
    }

    @Test
    void testRemoveInvalidProduct() {
        Long cartId = shoppingCart.getId();
        Long invalidProductId = 0L;

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartProductService.removeProduct(cartId, invalidProductId));
    }
}
