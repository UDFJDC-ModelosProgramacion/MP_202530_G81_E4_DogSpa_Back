package co.edu.udistrital.mdp.back.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.udistrital.mdp.back.entities.ReservationEntity;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    int countByService_IdAndReservationStatus(Long serviceId, String status);

    java.util.List<ReservationEntity> findByUserId(Long userId);
}
