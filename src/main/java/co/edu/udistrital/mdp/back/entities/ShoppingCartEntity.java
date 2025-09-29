package co.edu.udistrital.mdp.back.entities;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class ShoppingCartEntity extends BaseEntity {
    private double total;

    @PodamExclude
    @ManyToMany(mappedBy = "shoppingCarts") 
    private List<ProductEntity> products = new ArrayList<>();

    @PodamExclude
    @OneToOne
    private UserEntity user;

    @PodamExclude
    @OneToMany(mappedBy = "shoppingCart")
    private List<PaymentEntity> payments = new ArrayList<>();
}
