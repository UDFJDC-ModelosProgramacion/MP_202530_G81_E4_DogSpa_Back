package co.edu.udistrital.mdp.back.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long>{
    
}
