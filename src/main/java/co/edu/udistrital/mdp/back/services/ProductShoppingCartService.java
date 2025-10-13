package co.edu.udistrital.mdp.back.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.entities.ShoppingCartEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import co.edu.udistrital.mdp.back.repositories.ShoppingCartRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductShoppingCartService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Transactional
    public ShoppingCartEntity addShoppingCart(Long productId, Long shoppingCartId) 
            throws EntityNotFoundException {
        log.info("Starting process to associate shopping cart with id = {} to product with id = {}", 
                shoppingCartId, productId);
        
        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        if (productEntity.isEmpty())
            throw new EntityNotFoundException("Product with id = " + productId + " not found");

        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found");

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
            throw new EntityNotFoundException("Product with id = " + productId + " not found");

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
            throw new EntityNotFoundException("Product with id = " + productId + " not found");

        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found");

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
            throw new EntityNotFoundException("Product with id = " + productId + " not found");

        for (ShoppingCartEntity cart : shoppingCarts) {
            Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(cart.getId());
            if (shoppingCartEntity.isEmpty())
                throw new EntityNotFoundException("Shopping cart with id = " + cart.getId() + " not found");
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
            throw new EntityNotFoundException("Product with id = " + productId + " not found");

        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found");

        productEntity.get().getShoppingCarts().remove(shoppingCartEntity.get());
        shoppingCartEntity.get().getProducts().remove(productEntity.get());
        
        productRepository.save(productEntity.get());

        log.info("Finishing process to remove shopping cart with id = {} from product with id = {}", 
                shoppingCartId, productId);
    }
}