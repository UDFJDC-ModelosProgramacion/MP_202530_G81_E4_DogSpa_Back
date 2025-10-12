package co.edu.udistrital.mdp.back.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import uk.co.jemos.podam.common.PodamExclude;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
@Entity
public class UserEntity extends PersonEntity {

    private Integer loyaltypoints;

    @PodamExclude
    @OneToMany(mappedBy = "user")
    private List<ReservationEntity> reservation;

    @PodamExclude
    @OneToOne(mappedBy = "user")
    private ShoppingCartEntity shoppingCart;

    @PodamExclude
    @ManyToMany
    private List<NotificationEntity> notifications = new ArrayList<>();

    @PodamExclude
    @OneToMany(mappedBy = "user")
    private List<OrderEntity> orders = new ArrayList<>();
}
