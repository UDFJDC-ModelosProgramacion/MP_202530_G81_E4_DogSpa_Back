package co.edu.udistrital.mdp.back.dto;
import java.util.Date;
import co.edu.udistrital.mdp.back.entities.OrderStatus;
import lombok.Data;

@Data
public class OrderDTO {
    private Long id;
    private Date orderDate;
    private OrderStatus status;
    private Double totalAmount;
    private Double discount;

}