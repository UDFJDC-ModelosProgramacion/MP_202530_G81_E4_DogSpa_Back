package co.edu.udistrital.mdp.back.dto;
import lombok.Data;
@Data

public class PaymentDTO {
    private Long id;
    private double amount;
    private String method;
    private LocalDateTime date;
    private String status;
}
