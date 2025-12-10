package co.edu.udistrital.mdp.back.dto;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;


@Data
public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer duration;
    private List<MultimediaDTO> multimedia = new ArrayList<>();
}
