package co.edu.udistrital.mdp.back.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationDTO {
    private Long id;
    private LocalDate reservationDate;
    private String reservationStatus;
    private LocalTime startTime;
    private LocalTime endTime;
}
