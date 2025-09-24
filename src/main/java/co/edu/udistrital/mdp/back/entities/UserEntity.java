package co.edu.udistrital.mdp.back.entities;

import jakarta.persistence.Entity;
import lombok.Data;
    @Data
    @Entity
public class UserEntity extends PersonEntity{
    private Integer loyaltypoints;
    //ReservaEntity
}
