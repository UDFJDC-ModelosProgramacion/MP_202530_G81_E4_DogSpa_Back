package co.edu.udistrital.mdp.back.dto;
import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String category;
    private String description;
    private Double price;
    private Integer stock;
}
