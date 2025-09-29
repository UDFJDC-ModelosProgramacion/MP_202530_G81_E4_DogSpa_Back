package co.edu.udistrital.mdp.back.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import uk.co.jemos.podam.common.PodamExclude;
import java.util.List;
import lombok.Data;
    @Data
    @Entity
public class UserEntity extends PersonEntity{
    private Integer loyaltypoints;
    @PodamExclude
    @OneToMany(mappedBy = "user")
    private List<ReservationEntity> reservas;

    @PodamExclude
    @OneToMany(mappedBy = "user")
    private List<ShoppingCartEntity> carritos;
}
