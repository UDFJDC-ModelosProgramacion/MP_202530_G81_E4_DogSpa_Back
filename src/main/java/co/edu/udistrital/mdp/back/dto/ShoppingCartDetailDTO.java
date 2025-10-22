package co.edu.udistrital.mdp.back.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShoppingCartDetailDTO extends ShoppingCartDTO {
    private List<PaymentDTO> payments = new ArrayList<>();
    private List<ProductDTO> products = new ArrayList<>();
}