package co.edu.udistrital.mdp.back.entities;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import lombok.Data;


@Data
@Entity
public class ReviewEntity extends BaseEntity {
    private Integer rating;
    private String comments;
    private LocalDate reviewDate;

    
}
