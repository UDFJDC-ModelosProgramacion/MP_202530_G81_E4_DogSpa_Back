package co.edu.udistrital.mdp.back.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository; 
import co.edu.udistrital.mdp.back.entities.BranchEntity;

@Repository
public interface BranchRepository extends JpaRepository<BranchEntity, Long> {
}
