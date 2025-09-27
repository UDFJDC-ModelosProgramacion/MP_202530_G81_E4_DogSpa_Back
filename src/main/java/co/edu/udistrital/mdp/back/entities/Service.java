package co.edu.udistrital.mdp.back.entities;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Service extends BaseEntity {
    private String name;
    private String description;
    private Double price;
    private Integer duration;
    /*
    private Review review;
    private Multimedia multimedia;
     */

}
