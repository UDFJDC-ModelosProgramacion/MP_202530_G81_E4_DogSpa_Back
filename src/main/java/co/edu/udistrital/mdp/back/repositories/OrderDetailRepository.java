package co.edu.udistrital.mdp.back.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;

@Repository
public interface  OrderDetailRepository extends JpaRepository<OrderDetailEntity, Long>{

    // Cuenta cuántos OrderDetail hay para un producto específico
    int countByProductId(Long productId);

    // Cuenta la cantidad reservada de un producto en órdenes abiertas
    @Query("SELECT SUM(od.quantity) FROM OrderDetailEntity od WHERE od.product.id = :productId AND od.order.status = 'OPEN'")
    Integer countReservedForProduct(Long productId);
}
