package co.edu.udistrital.mdp.back.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Entity
@Data
public class OrderDetailEntity extends BaseEntity {
    
    private Integer quantity;
    private Double subtotal;

    @PodamExclude
    @ManyToOne
    private ProductEntity product;

    @PodamExclude
    @ManyToOne
    private OrderEntity order;
    
}
