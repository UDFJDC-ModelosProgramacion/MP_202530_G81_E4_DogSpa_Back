package co.edu.udistrital.mdp.back.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDtlDTO extends OrderDTO {
    private List<OrderDetailDTO> orderDetails = new ArrayList<>();
}