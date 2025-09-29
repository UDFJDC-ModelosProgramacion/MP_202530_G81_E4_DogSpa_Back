package co.edu.udistrital.mdp.back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.udistrital.mdp.back.entities.ShoppingCartEntity;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartEntity, Long> {
    
}
