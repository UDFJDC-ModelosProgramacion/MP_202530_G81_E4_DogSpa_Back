package co.edu.udistrital.mdp.back.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Data
@Entity
public class ReservationEntity extends BaseEntity {

    private LocalDate reservationDate;
    private String reservationStatus;
    private LocalTime startTime;
    private LocalTime endTime;

    @PodamExclude
    @ManyToOne
    private BranchEntity branch;

    @ManyToOne
    private UserEntity user;

    
}
