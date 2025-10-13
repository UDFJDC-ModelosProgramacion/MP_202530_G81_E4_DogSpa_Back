package co.edu.udistrital.mdp.back.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import co.edu.udistrital.mdp.back.entities.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long>{
    
    List<NotificationEntity> findByUsers_Id(Long userId);
    List<NotificationEntity> findByUsers_IdAndReadFalse(Long userId);
}