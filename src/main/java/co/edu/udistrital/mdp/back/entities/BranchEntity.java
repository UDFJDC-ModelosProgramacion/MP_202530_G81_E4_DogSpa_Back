package co.edu.udistrital.mdp.back.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Data
@Entity
public class BranchEntity extends BaseEntity {
    private String name;
    private String address;
    private String phone;
    private String service;
    private String zone;

    @PodamExclude
    @OneToMany(mappedBy = "branch")
    private List<ReservationEntity> reservations = new ArrayList<>();
}
