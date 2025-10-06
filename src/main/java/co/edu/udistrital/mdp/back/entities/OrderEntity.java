package co.edu.udistrital.mdp.back.entities;
import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class OrderEntity extends BaseEntity {

    private String status;
    private Date orderDate;
    private Double totalAmount;
    private double discount;

    @ManyToOne
    private UserEntity user;
}
