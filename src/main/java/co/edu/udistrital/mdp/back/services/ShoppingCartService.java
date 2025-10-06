package co.edu.udistrital.mdp.back.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.entities.ShoppingCartEntity;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import co.edu.udistrital.mdp.back.repositories.ShoppingCartRepository;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for ShoppingCart business logic
 * 
 * @author Alexander Morales Ujueta
 */
@Slf4j
@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Returns all shopping carts
     * 
     * @return List of all shopping carts
     */
    @Transactional
    public List<ShoppingCartEntity> getShoppingCarts() {
        log.info("Starting process to query all shopping carts");
        return shoppingCartRepository.findAll();
    }

    /**
     * Finds a shopping cart by ID
     * 
     * @param shoppingCartId The ID of the shopping cart to find
     * @return The shopping cart found
     * @throws EntityNotFoundException If the shopping cart does not exist
     */
    @Transactional
    public ShoppingCartEntity getShoppingCart(Long shoppingCartId) throws EntityNotFoundException {
        log.info("Starting process to query shopping cart with id = {0}", shoppingCartId);
        ShoppingCartEntity shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found"));
        log.info("Finishing process to query shopping cart with id = {0}", shoppingCartId);
        return shoppingCartEntity;
    }

    /**
     * Creates a new shopping cart
     * 
     * Business Rule: Cannot create a cart without an associated user
     * 
     * @param shoppingCartEntity The shopping cart to create
     * @return The created shopping cart
     * @throws IllegalOperationException If there is no associated user
     * @throws EntityNotFoundException If the user does not exist
     */
    @Transactional
    public ShoppingCartEntity createShoppingCart(ShoppingCartEntity shoppingCartEntity)
            throws IllegalOperationException, EntityNotFoundException {
        log.info("Starting process to create shopping cart");

        if (shoppingCartEntity.getUser() == null || shoppingCartEntity.getUser().getId() == null) {
            throw new IllegalOperationException("Cannot create a shopping cart without an associated user");
        }

        UserEntity userEntity = userRepository.findById(shoppingCartEntity.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with id = " + shoppingCartEntity.getUser().getId() + " not found"));

        if (userEntity.getShoppingCart() != null) {
            throw new IllegalOperationException("User already has an associated shopping cart");
        }

        shoppingCartEntity.setUser(userEntity);
        log.info("Finishing process to create shopping cart");
        return shoppingCartRepository.save(shoppingCartEntity);
    }

    /**
     * Updates a shopping cart
     * 
     * Business Rule: Cannot add non-existent products to cart
     * 
     * @param shoppingCartId The ID of the shopping cart to update
     * @param shoppingCart The shopping cart with updated data
     * @return The updated shopping cart
     * @throws EntityNotFoundException If the shopping cart does not exist
     * @throws IllegalOperationException If attempting to add non-existent products
     */
    @Transactional
    public ShoppingCartEntity updateShoppingCart(Long shoppingCartId, ShoppingCartEntity shoppingCart)
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to update shopping cart with id = {0}", shoppingCartId);

        ShoppingCartEntity shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found"));

        if (shoppingCart.getProducts() != null && !shoppingCart.getProducts().isEmpty()) {
            for (ProductEntity product : shoppingCart.getProducts()) {
                if (product.getId() == null) {
                    throw new IllegalOperationException("Cannot add a product without ID to cart");
                }
                productRepository.findById(product.getId())
                        .orElseThrow(() -> new IllegalOperationException(
                                "Cannot add non-existent product with id = " + product.getId() + " to cart"));
            }
        }

        shoppingCartEntity.setTotal(shoppingCart.getTotal());
        shoppingCartEntity.setProducts(shoppingCart.getProducts());

        log.info("Finishing process to update shopping cart with id = {0}", shoppingCartId);
        return shoppingCartRepository.save(shoppingCartEntity);
    }

    /**
     * Deletes a shopping cart
     * 
     * Business Rule: Cannot delete a cart if it has an active order
     * 
     * @param shoppingCartId The ID of the shopping cart to delete
     * @throws EntityNotFoundException If the shopping cart does not exist
     * @throws IllegalOperationException If it has an active order (associated payments)
     */
    @Transactional
    public void deleteShoppingCart(Long shoppingCartId) throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to delete shopping cart with id = {0}", shoppingCartId);

        ShoppingCartEntity shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found"));

        if (shoppingCartEntity.getPayments() != null && !shoppingCartEntity.getPayments().isEmpty()) {
            throw new IllegalOperationException("Cannot delete shopping cart because it has associated payments/orders");
        }

        shoppingCartRepository.delete(shoppingCartEntity);
        log.info("Finishing process to delete shopping cart with id = {0}", shoppingCartId);
    }

    /**
     * Adds a product to the shopping cart
     * 
     * @param shoppingCartId The ID of the shopping cart
     * @param productId The ID of the product to add
     * @return The updated shopping cart
     * @throws EntityNotFoundException If cart or product does not exist
     * @throws IllegalOperationException If product does not exist
     */
    @Transactional
    public ShoppingCartEntity addProductToCart(Long shoppingCartId, Long productId)
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to add product with id = {0} to cart with id = {1}", productId, shoppingCartId);

        ShoppingCartEntity cart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found"));

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalOperationException(
                        "Cannot add non-existent product with id = " + productId + " to cart"));

        cart.getProducts().add(product);
        double newTotal = cart.getTotal() + product.getPrice();
        cart.setTotal(newTotal);

        log.info("Finishing process to add product to cart");
        return shoppingCartRepository.save(cart);
    }

    /**
     * Removes a product from the shopping cart
     * 
     * @param shoppingCartId The ID of the shopping cart
     * @param productId The ID of the product to remove
     * @return The updated shopping cart
     * @throws EntityNotFoundException If cart or product does not exist
     */
    @Transactional
    public ShoppingCartEntity removeProductFromCart(Long shoppingCartId, Long productId)
            throws EntityNotFoundException {
        log.info("Starting process to remove product with id = {0} from cart with id = {1}", productId, shoppingCartId);

        ShoppingCartEntity cart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found"));

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id = " + productId + " not found"));

        cart.getProducts().remove(product);
        double newTotal = cart.getTotal() - product.getPrice();
        cart.setTotal(Math.max(0, newTotal));

        log.info("Finishing process to remove product from cart");
        return shoppingCartRepository.save(cart);
    }
}