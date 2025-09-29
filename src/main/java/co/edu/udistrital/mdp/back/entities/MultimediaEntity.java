package co.edu.udistrital.mdp.back.entities;

import jakarta.persistence.*;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Data
@Entity
public class MultimediaEntity extends BaseEntity {
    private String type;
    private String url;
    
    @PodamExclude
    @ManyToOne
    private BranchEntity branch;
    
    @PodamExclude
    @ManyToOne
    private ServiceEntity service;
    
    @PodamExclude
    @ManyToOne
    private ProductEntity product;
}
