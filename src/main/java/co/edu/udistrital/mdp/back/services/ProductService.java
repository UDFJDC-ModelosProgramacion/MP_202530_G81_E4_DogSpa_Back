package co.edu.udistrital.mdp.back.services;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import jakarta.transaction.Transactional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    private static final String productNotFoundMessage = "Product not found";

    public List<ProductEntity> findAll() {
        return productRepository.findAll();
    }
    
    public Optional<ProductEntity> findById(Long id) {
        return productRepository.findById(id);
    }
    public ProductEntity getByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(productNotFoundMessage));
    }

    @Transactional
    public ProductEntity save(ProductEntity product) {
        validatePrice(product.getPrice());
        product.setPrice(roundToTwoDecimals(product.getPrice()));
        return productRepository.save(product);
    }

    @Transactional
    public ProductEntity update(Long productId, ProductEntity incoming) {
        ProductEntity current = getById(productId);

        if (incoming.getName() != null)        current.setName(incoming.getName());
        if (incoming.getDescription() != null) current.setDescription(incoming.getDescription());

        if (incoming.getPrice() != null) {
            validatePrice(incoming.getPrice());
            current.setPrice(roundToTwoDecimals(incoming.getPrice()));
        }
       
        if (incoming.getStock() != null) {
            int newStock = incoming.getStock();
            int reserved = orderDetailRepository.countReservedForProduct(productId);
            if (newStock < reserved) {
                throw new IllegalArgumentException("Stock cannot be less than reserved quantity in open orders");
            }
            current.setStock(newStock);
        }
        return productRepository.save(current);
    }

    @Transactional
    public ProductEntity updateStock(Long productId, int newStock) {
        ProductEntity product = getByIdOrThrow(productId);

        int reserved = orderDetailRepository.countReservedForProduct(productId);
        if (newStock < reserved) {
            throw new IllegalArgumentException("Stock cannot be less than reserved quantity in open orders");
        }
        product.setStock(newStock);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteById(Long id) {
        int count = orderDetailRepository.countByProductId(id);
        if (count > 0) {
            throw new IllegalStateException("Cannot delete product with associated order details");
        }
        productRepository.deleteById(id);
    }

    private void validatePrice(Double price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price must be ≥ 0");
        }
        BigDecimal bd = BigDecimal.valueOf(price);
        if (bd.scale() > 2) {
            throw new IllegalArgumentException("Price must have at most 2 decimal places");
        }
    }

    private Double roundToTwoDecimals(Double value) {
        if (value == null) return null;
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
