package co.edu.udistrital.mdp.back.entities;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Entity
@Data
public class ServiceEntity extends BaseEntity {
    
    private String name;
    private String description;
    private Double price;
    private Integer duration;
    /*
    private Review review;
    private Multimedia multimedia;
     */

    @PodamExclude
    @ManyToMany
    private List<BranchEntity> branches = new ArrayList<>();

}
