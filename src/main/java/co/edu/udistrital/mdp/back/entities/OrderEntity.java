package co.edu.udistrital.mdp.back.entities;
import java.sql.Date;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class OrderEntity extends BaseEntity {

    private String status;
    private Date orderDate;
    private Double totalAmount;
    private double discount;

    @OneToMany(mappedBy = "order")
    private List<OrderDetailEntity> orderDetails;
    
    @ManyToOne
    private UserEntity user;
}
