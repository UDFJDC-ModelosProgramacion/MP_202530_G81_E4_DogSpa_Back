package co.edu.udistrital.mdp.back.services;

import java.util.List;
import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.ReservationEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.ReservationRepository;
import co.edu.udistrital.mdp.back.repositories.BranchRepository;
import co.edu.udistrital.mdp.back.repositories.ServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final String RESERVATION_NOT_FOUND_MESSAGE = "No reservation found with ID: ";

    private final ReservationRepository reservationRepository;
    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public ReservationEntity createReservation(@Valid ReservationEntity reservation)
            throws IllegalOperationException {
        validateReservationTimes(reservation);
        // Resolve branch and service entities if only ID provided
        if (reservation.getBranch() != null && reservation.getBranch().getId() != null) {
            var branch = branchRepository.findById(reservation.getBranch().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Branch not found with id " + reservation.getBranch().getId()));
            reservation.setBranch(branch);
        }

        if (reservation.getService() != null && reservation.getService().getId() != null) {
            var service = serviceRepository.findById(reservation.getService().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Service not found with id " + reservation.getService().getId()));
            reservation.setService(service);
        }

        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationEntity> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ReservationEntity getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RESERVATION_NOT_FOUND_MESSAGE + id));
    }

    @Transactional
    public ReservationEntity updateReservation(Long id, @Valid ReservationEntity reservation)
            throws IllegalOperationException {
        ReservationEntity existing = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RESERVATION_NOT_FOUND_MESSAGE + id));
        
        // Validate times before updating
        if (reservation.getStartTime() != null && reservation.getEndTime() != null
                && reservation.getEndTime().isBefore(reservation.getStartTime())) {
            throw new IllegalOperationException("End time must be after start time.");
        }

        existing.setReservationDate(reservation.getReservationDate());
        existing.setReservationStatus(reservation.getReservationStatus());
        existing.setStartTime(reservation.getStartTime());
        existing.setEndTime(reservation.getEndTime());
        existing.setBranch(reservation.getBranch());
        existing.setUser(reservation.getUser());

        return reservationRepository.save(existing);
    }

    @Transactional
    public void deleteReservation(Long id) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RESERVATION_NOT_FOUND_MESSAGE + id));
        reservationRepository.delete(reservation);
    }

    private void validateReservationTimes(ReservationEntity reservation)
            throws IllegalOperationException {
        if (reservation.getStartTime() != null && reservation.getEndTime() != null
                && reservation.getEndTime().isBefore(reservation.getStartTime())) {
            throw new IllegalOperationException("End time must be after start time.");
        }
    }
}