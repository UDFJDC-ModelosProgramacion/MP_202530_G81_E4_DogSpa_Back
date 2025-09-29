package co.edu.udistrital.mdp.back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.udistrital.mdp.back.entities.ReviewEntity;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    
}
