package co.edu.udistrital.mdp.back.entities;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Entity
@Data
public class OrderEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private Date orderDate;
    private Double totalAmount;
    private double discount;

    @PodamExclude
    @OneToMany(mappedBy = "order")
    private List<OrderDetailEntity> orderDetails = new ArrayList<>();

    @ManyToOne
    private UserEntity user;
}