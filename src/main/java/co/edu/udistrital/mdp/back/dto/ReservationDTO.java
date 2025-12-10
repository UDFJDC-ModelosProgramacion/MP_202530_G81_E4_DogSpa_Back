package co.edu.udistrital.mdp.back.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class ReservationDTO {
    private Long id;
    private LocalDate reservationDate;
    private String reservationStatus;
    private LocalTime startTime;
    private LocalTime endTime;

    // optional related info for frontend convenience
    private BranchDTO branch;
    private ServiceDTO service;
    private String petName;
    // also expose foreign keys so frontend can fetch related info if nested objects
    // are missing
    private Long branchId;
    private Long serviceId;
    private Long userId;
}
