package co.edu.udistrital.mdp.back.dto;
import lombok.Data;

@Data
public class OrderDetailDTO {
    private Long id;
    private Integer quantity;
    private Double subtotal;
    private ProductDTO product;  // Incluir producto en la respuesta
}
