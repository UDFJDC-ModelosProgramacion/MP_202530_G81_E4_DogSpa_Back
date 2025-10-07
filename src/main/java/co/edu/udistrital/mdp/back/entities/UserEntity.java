package co.edu.udistrital.mdp.back.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import uk.co.jemos.podam.common.PodamExclude;

import java.util.List;

import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.ManyToMany;
import lombok.Data;
    @Data
    @Entity
public class UserEntity extends PersonEntity{
    private String username;
    private Integer loyaltypoints;
    @PodamExclude
    @OneToMany(mappedBy = "user")
    private List<ReservationEntity> reservation;

    @PodamExclude
    @OneToOne(mappedBy = "user")
    private ShoppingCartEntity shoppingCart;

    @PodamExclude
    @ManyToMany
    private List<NotificationEntity> notifications;
}
