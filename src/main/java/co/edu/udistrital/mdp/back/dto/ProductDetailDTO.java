package co.edu.udistrital.mdp.back.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDetailDTO extends ProductDTO {
    private List<OrderDetailDTO> orderDetails = new ArrayList<>();
    private List<MultimediaDTO> multimedia = new ArrayList<>();
    private List<ShoppingCartDTO> shoppingCarts = new ArrayList<>();
}