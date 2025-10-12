package co.edu.udistrital.mdp.back.entities;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Data
@Entity
public class BranchEntity extends BaseEntity {

    private String name;
    private String address;
    private String phone;
    private String zone;

    @PodamExclude
    @OneToMany(mappedBy = "branch")
    private List<ReservationEntity> reservations = new ArrayList<>();

    @PodamExclude
    @ManyToMany(mappedBy = "branches")
    private List<ServiceEntity> services = new ArrayList<>();

    @PodamExclude
    @OneToMany(mappedBy = "branch")
    private List<MultimediaEntity> multimedia = new ArrayList<>();
}
