package co.edu.udistrital.mdp.back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.udistrital.mdp.back.entities.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    
}