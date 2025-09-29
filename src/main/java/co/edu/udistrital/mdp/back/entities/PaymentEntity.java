package co.edu.udistrital.mdp.back.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Data
@Entity
@Table(name = "payment")
public class PaymentEntity extends BaseEntity {
    private double amount;
    private String method;
    private LocalDateTime date;
    private String status;

    @PodamExclude
    @ManyToOne
    @JoinColumn(name = "shopping_cart_id")
    private ShoppingCartEntity shoppingCart;
}
