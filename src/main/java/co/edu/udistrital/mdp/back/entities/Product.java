package co.edu.udistrital.mdp.back.entities;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Product extends BaseEntity {
    private String name;
    private String category;
    private String description;
    private Integer popularity;
    private Double price;
    /*
    private Review review;
    private Multimedia multimedia;
     */

}
