package co.edu.udistrital.mdp.back.entities;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Entity
@Data
public class NotificationEntity extends BaseEntity {
    private String message;
    private Date date;
    private Boolean read;

    @PodamExclude
    @ManyToMany(mappedBy= "notifications")
    private List<UserEntity> users = new ArrayList<>();

}
