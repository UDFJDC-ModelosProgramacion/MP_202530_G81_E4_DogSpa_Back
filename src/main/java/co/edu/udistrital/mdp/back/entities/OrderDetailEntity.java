package co.edu.udistrital.mdp.back.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class OrderDetailEntity extends BaseEntity {
    
    private Integer quantity;
    private Double subtotal;

    @ManyToOne
    private ProductEntity product;

    @ManyToOne
    private OrderEntity order;
    
}
