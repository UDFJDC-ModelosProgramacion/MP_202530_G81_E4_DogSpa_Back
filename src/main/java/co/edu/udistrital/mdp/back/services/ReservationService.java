package co.edu.udistrital.mdp.back.services;

import java.util.List;

import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.ReservationEntity;
import co.edu.udistrital.mdp.back.repositories.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;


    public ReservationEntity createReservation(@Valid ReservationEntity reservation) {
        validateReservationTimes(reservation);
        return reservationRepository.save(reservation);
    }

  
    @Transactional(readOnly = true)
    public List<ReservationEntity> getAllReservations() {
        return reservationRepository.findAll();
    }


    @Transactional(readOnly = true)
    public ReservationEntity getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una reserva con el ID: " + id));
    }

    public ReservationEntity updateReservation(Long id, @Valid ReservationEntity reservation) {
        ReservationEntity existing = getReservationById(id);
        validateReservationTimes(reservation);

        existing.setReservationDate(reservation.getReservationDate());
        existing.setReservationStatus(reservation.getReservationStatus());
        existing.setStartTime(reservation.getStartTime());
        existing.setEndTime(reservation.getEndTime());
        existing.setBranch(reservation.getBranch());
        existing.setUser(reservation.getUser());

        return reservationRepository.save(existing);
    }

    public void deleteReservation(Long id) {
        ReservationEntity reservation = getReservationById(id);
        reservationRepository.delete(reservation);
    }

    private void validateReservationTimes(ReservationEntity reservation) {
        if (reservation.getStartTime() != null && reservation.getEndTime() != null
                && reservation.getEndTime().isBefore(reservation.getStartTime())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }
    }
}
