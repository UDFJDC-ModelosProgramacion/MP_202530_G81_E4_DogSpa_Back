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
@Import(ShoppingCartService.class)
class ShoppingCartServiceTest {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private List<ShoppingCartEntity> cartList = new ArrayList<>();
    private List<UserEntity> userList = new ArrayList<>();
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
        for (int i = 0; i < 3; i++) {
            ProductEntity product = factory.manufacturePojo(ProductEntity.class);
            product.setPrice(50.0 + i * 25);
            entityManager.persist(product);
            productList.add(product);
        }

        for (int i = 0; i < 3; i++) {
            UserEntity user = factory.manufacturePojo(UserEntity.class);
            entityManager.persist(user);
            userList.add(user);

            ShoppingCartEntity cart = factory.manufacturePojo(ShoppingCartEntity.class);
            cart.setUser(user);
            cart.setTotal(0.0);
            entityManager.persist(cart);
            cartList.add(cart);

            user.setShoppingCart(cart);
        }
    }

    @Test
    void testCreateShoppingCart() throws EntityNotFoundException, IllegalOperationException {
        UserEntity newUser = factory.manufacturePojo(UserEntity.class);
        entityManager.persist(newUser);

        ShoppingCartEntity newCart = factory.manufacturePojo(ShoppingCartEntity.class);
        newCart.setUser(newUser);
        newCart.setTotal(0.0);

        ShoppingCartEntity result = shoppingCartService.createShoppingCart(newCart);
        assertNotNull(result);

        ShoppingCartEntity entity = entityManager.find(ShoppingCartEntity.class, result.getId());
        assertEquals(newUser.getId(), entity.getUser().getId());
    }

    @Test
    void testCreateShoppingCartWithoutUser() {
        ShoppingCartEntity newCart = factory.manufacturePojo(ShoppingCartEntity.class);
        newCart.setUser(null);
        assertThrows(IllegalOperationException.class,
                () -> shoppingCartService.createShoppingCart(newCart)
        );
    }

    @Test
    void testCreateShoppingCartWithInvalidUser() {
        ShoppingCartEntity newCart = factory.manufacturePojo(ShoppingCartEntity.class);
        UserEntity invalidUser = new UserEntity();
        invalidUser.setId(0L);
        newCart.setUser(invalidUser);

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.createShoppingCart(newCart)
        );
    }

    @Test
    void testCreateShoppingCartForUserWithCart() {
        UserEntity user = userList.get(0);
        ShoppingCartEntity newCart = factory.manufacturePojo(ShoppingCartEntity.class);
        newCart.setUser(user);
        assertThrows(IllegalOperationException.class, () -> {
            try {
                shoppingCartService.createShoppingCart(newCart);
            } catch (EntityNotFoundException e) {
                fail("Should not throw EntityNotFoundException");
            }
        });
    }

    @Test
    void testGetShoppingCarts() {
        List<ShoppingCartEntity> list = shoppingCartService.getShoppingCarts();
        assertEquals(cartList.size(), list.size());
    }

    @Test
    void testGetShoppingCart() throws EntityNotFoundException {
        ShoppingCartEntity entity = cartList.get(0);
        ShoppingCartEntity result = shoppingCartService.getShoppingCart(entity.getId());
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
    }

    @Test
    void testGetInvalidShoppingCart() {
        assertThrows(EntityNotFoundException.class, () -> shoppingCartService.getShoppingCart(0L));
    }

    @Test
    void testUpdateShoppingCart() throws EntityNotFoundException, IllegalOperationException {
        ShoppingCartEntity entity = cartList.get(0);
        ShoppingCartEntity updateEntity = new ShoppingCartEntity();
        updateEntity.setTotal(150.0);
        updateEntity.setProducts(new ArrayList<>());

        ShoppingCartEntity result = shoppingCartService.updateShoppingCart(entity.getId(), updateEntity);

        assertEquals(150.0, result.getTotal());
    }

    @Test
    void testUpdateShoppingCartWithInvalidProduct() {
        ShoppingCartEntity entity = cartList.get(0);
        ShoppingCartEntity updateEntity = new ShoppingCartEntity();

        ProductEntity invalidProduct = new ProductEntity();
        invalidProduct.setId(0L);

        List<ProductEntity> products = new ArrayList<>();
        products.add(invalidProduct);
        updateEntity.setProducts(products);

        Long cartId = entity.getId();

        assertThrows(IllegalOperationException.class,
                () -> shoppingCartService.updateShoppingCart(cartId, updateEntity)
        );
    }

    @Test
    void testUpdateInvalidShoppingCart() {
        ShoppingCartEntity updateEntity = new ShoppingCartEntity();
        updateEntity.setTotal(100.0);
        assertThrows(EntityNotFoundException.class, () -> {
            try {
                shoppingCartService.updateShoppingCart(0L, updateEntity);
            } catch (IllegalOperationException e) {
                fail("Should not throw IllegalOperationException");
            }
        });
    }

    @Test
    void testDeleteShoppingCart() throws EntityNotFoundException, IllegalOperationException {
        ShoppingCartEntity entity = cartList.get(0);
        shoppingCartService.deleteShoppingCart(entity.getId());

        ShoppingCartEntity deleted = entityManager.find(ShoppingCartEntity.class, entity.getId());
        assertNull(deleted);
    }

    @Test
    void testDeleteInvalidShoppingCart() {
        assertThrows(EntityNotFoundException.class, () -> shoppingCartService.deleteShoppingCart(0L));
    }

    @Test
    void testAddProductToCart() throws EntityNotFoundException, IllegalOperationException {
        ShoppingCartEntity cart = cartList.get(0);
        ProductEntity product = productList.get(0);

        ShoppingCartEntity result = shoppingCartService.addProductToCart(cart.getId(), product.getId());

        assertTrue(result.getProducts().contains(product));
        assertEquals(product.getPrice(), result.getTotal());
    }

    @Test
    void testAddInvalidProductToCart() {
        ShoppingCartEntity cart = cartList.get(0);
        Long cartId = cart.getId();
        long invalidProductId = 0L;

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.addProductToCart(cartId, invalidProductId)
        );
    }

    @Test
    void testAddProductToInvalidCart() {
        ProductEntity product = productList.get(0);
        Long productId = product.getId();
        long invalidCartId = 0L;

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.addProductToCart(invalidCartId, productId)
        );
    }

    @Test
    void testRemoveProductFromCart() throws EntityNotFoundException, IllegalOperationException {
        ShoppingCartEntity cart = cartList.get(0);
        ProductEntity product = productList.get(0);

        // Primero añadir el producto
        cart.getProducts().add(product);
        cart.setTotal(product.getPrice());
        entityManager.persist(cart);

        // Luego removerlo
        ShoppingCartEntity result = shoppingCartService.removeProductFromCart(cart.getId(), product.getId());

        assertFalse(result.getProducts().contains(product));
        assertEquals(0.0, result.getTotal());
    }

    @Test
    void testRemoveInvalidProductFromCart() {
        ShoppingCartEntity cart = cartList.get(0);
        Long cartId = cart.getId();
        long invalidProductId = 0L;

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.removeProductFromCart(cartId, invalidProductId)
        );
    }

    @Test
    void testRemoveProductFromInvalidCart() {
        ProductEntity product = productList.get(0);
        Long productId = product.getId();
        long invalidCartId = 0L;

        assertThrows(EntityNotFoundException.class,
                () -> shoppingCartService.removeProductFromCart(invalidCartId, productId)
        );
    }
}
