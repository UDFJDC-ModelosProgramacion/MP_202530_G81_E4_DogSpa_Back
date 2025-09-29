package co.edu.udistrital.mdp.back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.udistrital.mdp.back.entities.MultimediaEntity;

public interface MultimediaRepository extends JpaRepository<MultimediaEntity, Long> {
    
}
