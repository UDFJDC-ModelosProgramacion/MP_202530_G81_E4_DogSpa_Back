package co.edu.udistrital.mdp.back.dto;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import co.edu.udistrital.mdp.back.entities.OrderStatus;
import lombok.Data;

@Data
public class OrderDTO {
    private Long id;
    private Date orderDate;
    private OrderStatus status;
    private Double totalAmount;
    private Double discount;
    private List<OrderDetailDTO> orderDetails = new ArrayList<>();
}
