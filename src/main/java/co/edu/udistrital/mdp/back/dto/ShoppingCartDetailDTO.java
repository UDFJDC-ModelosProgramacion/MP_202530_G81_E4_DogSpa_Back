package co.edu.udistrital.mdp.back.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;


@Data
public class ShoppingCartDetailDTO extends ShoppingCartDTO {
    private List<PaymentDTO> payments = new ArrayList<>();
    private List<ProductDTO> products = new ArrayList<>();

}
