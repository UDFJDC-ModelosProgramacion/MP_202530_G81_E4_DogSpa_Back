package co.edu.udistrital.mdp.back.dto;
import java.util.List;
import java.util.ArrayList;

public class ProductDetailDTO extends ProductDTO {
    private List<OrderDetailDTO> orderDetails = new ArrayList<>();
    private List<MultimediaDTO> multimedia = new ArrayList<>();
    private List<ShoppingCartDTO> shoppingCarts = new ArrayList<>();
}
