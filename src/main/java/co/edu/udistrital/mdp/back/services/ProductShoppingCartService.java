package co.edu.udistrital.mdp.back.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.entities.ShoppingCartEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import co.edu.udistrital.mdp.back.repositories.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor  // Genera el constructor con todos los campos final
public class ProductShoppingCartService {

    // Constantes para mensajes de error repetidos
    private static final String PRODUCT_NOT_FOUND = "Product with id = %d not found";
    private static final String SHOPPING_CART_NOT_FOUND = "Shopping cart with id = %d not found";

    // Campos final para inyección por constructor
    private final ProductRepository productRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    @Transactional
    public ShoppingCartEntity addShoppingCart(Long productId, Long shoppingCartId) 
            throws EntityNotFoundException {
        log.info("Starting process to associate shopping cart with id = {} to product with id = {}", 
                shoppingCartId, productId);
        
        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        if (productEntity.isEmpty())
            throw new EntityNotFoundException(String.format(PRODUCT_NOT_FOUND, productId));

        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException(String.format(SHOPPING_CART_NOT_FOUND, shoppingCartId));

        if (!productEntity.get().getShoppingCarts().contains(shoppingCartEntity.get())) {
            productEntity.get().getShoppingCarts().add(shoppingCartEntity.get());
            productRepository.save(productEntity.get());
        }

        log.info("Finishing process to associate shopping cart with id = {} to product with id = {}", 
                shoppingCartId, productId);
        return shoppingCartEntity.get();
    }

    @Transactional
    public List<ShoppingCartEntity> getShoppingCarts(Long productId) throws EntityNotFoundException {
        log.info("Starting process to query all shopping carts of product with id = {}", productId);
        
        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        if (productEntity.isEmpty())
            throw new EntityNotFoundException(String.format(PRODUCT_NOT_FOUND, productId));

        log.info("Finishing process to query all shopping carts of product with id = {}", productId);
        return productEntity.get().getShoppingCarts();
    }

    @Transactional
    public ShoppingCartEntity getShoppingCart(Long productId, Long shoppingCartId) 
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to query shopping cart with id = {} of product with id = {}", 
                shoppingCartId, productId);
        
        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        if (productEntity.isEmpty())
            throw new EntityNotFoundException(String.format(PRODUCT_NOT_FOUND, productId));

        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException(String.format(SHOPPING_CART_NOT_FOUND, shoppingCartId));

        log.info("Finishing process to query shopping cart with id = {} of product with id = {}", 
                shoppingCartId, productId);
        
        if (!productEntity.get().getShoppingCarts().contains(shoppingCartEntity.get()))
            throw new IllegalOperationException("The shopping cart is not associated to the product");

        return shoppingCartEntity.get();
    }

    @Transactional
    public List<ShoppingCartEntity> replaceShoppingCarts(Long productId, List<ShoppingCartEntity> shoppingCarts) 
            throws EntityNotFoundException {
        log.info("Starting process to replace shopping carts associated with product with id = {}", productId);
        
        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        if (productEntity.isEmpty())
            throw new EntityNotFoundException(String.format(PRODUCT_NOT_FOUND, productId));

        for (ShoppingCartEntity cart : shoppingCarts) {
            Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(cart.getId());
            if (shoppingCartEntity.isEmpty())
                throw new EntityNotFoundException(String.format(SHOPPING_CART_NOT_FOUND, cart.getId()));
        }

        productEntity.get().setShoppingCarts(shoppingCarts);
        productRepository.save(productEntity.get());

        log.info("Finishing process to replace shopping carts associated with product with id = {}", productId);
        return productEntity.get().getShoppingCarts();
    }

    @Transactional
    public void removeShoppingCart(Long productId, Long shoppingCartId) throws EntityNotFoundException {
        log.info("Starting process to remove shopping cart with id = {} from product with id = {}", 
                shoppingCartId, productId);
        
        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        if (productEntity.isEmpty())
            throw new EntityNotFoundException(String.format(PRODUCT_NOT_FOUND, productId));

        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException(String.format(SHOPPING_CART_NOT_FOUND, shoppingCartId));

        productEntity.get().getShoppingCarts().remove(shoppingCartEntity.get());
        shoppingCartEntity.get().getProducts().remove(productEntity.get());
        
        productRepository.save(productEntity.get());

        log.info("Finishing process to remove shopping cart with id = {} from product with id = {}", 
                shoppingCartId, productId);
    }
}