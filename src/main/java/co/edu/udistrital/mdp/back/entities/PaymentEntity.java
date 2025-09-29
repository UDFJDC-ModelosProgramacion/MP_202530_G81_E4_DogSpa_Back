package co.edu.udistrital.mdp.back.entities;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Data
@Entity
public class PaymentEntity extends BaseEntity {
    private double amount;
    private String method;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    private String status;

    @PodamExclude
    @ManyToOne
    private ShoppingCartEntity shoppingCart;
}
