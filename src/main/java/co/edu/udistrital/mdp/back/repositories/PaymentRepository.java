package co.edu.udistrital.mdp.back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.udistrital.mdp.back.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
}