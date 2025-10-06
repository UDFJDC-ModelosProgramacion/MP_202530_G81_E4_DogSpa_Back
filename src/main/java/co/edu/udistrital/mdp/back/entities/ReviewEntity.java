package co.edu.udistrital.mdp.back.entities;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;


@Data
@Entity
public class ReviewEntity extends BaseEntity {
    private Integer rating;
    private String comments;
    private LocalDate reviewDate;
    @PodamExclude
    @ManyToOne
    private ServiceEntity service;
    
}
