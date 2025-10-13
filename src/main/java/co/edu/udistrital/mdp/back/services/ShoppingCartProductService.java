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
public class ShoppingCartProductService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public ProductEntity addProduct(Long shoppingCartId, Long productId) 
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to associate product with id = {} to shopping cart with id = {}", 
                productId, shoppingCartId);
        
        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found");

        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        if (productEntity.isEmpty())
            throw new EntityNotFoundException("Product with id = " + productId + " not found");

        if (productEntity.get().getStock() <= 0) {
            throw new IllegalOperationException("Cannot add product without stock to cart");
        }

        if (!shoppingCartEntity.get().getProducts().contains(productEntity.get())) {
            shoppingCartEntity.get().getProducts().add(productEntity.get());
            double newTotal = shoppingCartEntity.get().getTotal() + productEntity.get().getPrice();
            shoppingCartEntity.get().setTotal(newTotal);
            shoppingCartRepository.save(shoppingCartEntity.get());
        }

        log.info("Finishing process to associate product with id = {} to shopping cart with id = {}", 
                productId, shoppingCartId);
        return productEntity.get();
    }

    @Transactional
    public List<ProductEntity> getProducts(Long shoppingCartId) throws EntityNotFoundException {
        log.info("Starting process to query all products of shopping cart with id = {}", shoppingCartId);
        
        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found");

        log.info("Finishing process to query all products of shopping cart with id = {}", shoppingCartId);
        return shoppingCartEntity.get().getProducts();
    }

    @Transactional
    public ProductEntity getProduct(Long shoppingCartId, Long productId) 
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to query product with id = {} of shopping cart with id = {}", 
                productId, shoppingCartId);
        
        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found");

        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        if (productEntity.isEmpty())
            throw new EntityNotFoundException("Product with id = " + productId + " not found");

        log.info("Finishing process to query product with id = {} of shopping cart with id = {}", 
                productId, shoppingCartId);
        
        if (!shoppingCartEntity.get().getProducts().contains(productEntity.get()))
            throw new IllegalOperationException("The product is not associated to the shopping cart");

        return productEntity.get();
    }

    @Transactional
    public List<ProductEntity> replaceProducts(Long shoppingCartId, List<ProductEntity> products) 
            throws EntityNotFoundException {
        log.info("Starting process to replace products associated with shopping cart with id = {}", 
                shoppingCartId);
        
        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found");

        double newTotal = 0.0;
        for (ProductEntity product : products) {
            Optional<ProductEntity> productEntity = productRepository.findById(product.getId());
            if (productEntity.isEmpty())
                throw new EntityNotFoundException("Product with id = " + product.getId() + " not found");
            
            newTotal += productEntity.get().getPrice();
        }

        shoppingCartEntity.get().setProducts(products);
        shoppingCartEntity.get().setTotal(newTotal);
        shoppingCartRepository.save(shoppingCartEntity.get());

        log.info("Finishing process to replace products associated with shopping cart with id = {}", 
                shoppingCartId);
        return shoppingCartEntity.get().getProducts();
    }

    @Transactional
    public void removeProduct(Long shoppingCartId, Long productId) throws EntityNotFoundException {
        log.info("Starting process to remove product with id = {} from shopping cart with id = {}", 
                productId, shoppingCartId);
        
        Optional<ShoppingCartEntity> shoppingCartEntity = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartEntity.isEmpty())
            throw new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found");

        Optional<ProductEntity> productEntity = productRepository.findById(productId);
        if (productEntity.isEmpty())
            throw new EntityNotFoundException("Product with id = " + productId + " not found");

        shoppingCartEntity.get().getProducts().remove(productEntity.get());
        
        double newTotal = shoppingCartEntity.get().getTotal() - productEntity.get().getPrice();
        shoppingCartEntity.get().setTotal(Math.max(0, newTotal));
        shoppingCartRepository.save(shoppingCartEntity.get());

        log.info("Finishing process to remove product with id = {} from shopping cart with id = {}", 
                productId, shoppingCartId);
    }
}