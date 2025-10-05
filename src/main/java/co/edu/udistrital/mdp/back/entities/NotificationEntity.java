package co.edu.udistrital.mdp.back.entities;
import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

import lombok.Data;

@Entity
@Data
public class NotificationEntity extends BaseEntity {
    private String message;
    private Date date;
    private Boolean read;

    @ManyToMany
    private UserEntity user;

}
