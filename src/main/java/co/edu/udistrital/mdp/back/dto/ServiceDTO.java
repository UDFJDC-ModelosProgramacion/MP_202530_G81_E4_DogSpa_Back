package co.edu.udistrital.mdp.back.dto;
import lombok.Data;


@Data
public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer duration;
}
