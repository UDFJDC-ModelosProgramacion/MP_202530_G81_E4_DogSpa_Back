package co.edu.udistrital.mdp.back.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ReviewDTO {
    private Long id;
    private Integer rating;
    private String comments;
    private LocalDate reviewDate;
}
