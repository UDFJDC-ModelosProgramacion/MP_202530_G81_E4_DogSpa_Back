package co.edu.udistrital.mdp.back.dto;
import java.util.Date;
import lombok.Data;

@Data
public class NotificationDTO {
    private Long id;
    private String message;
    private Boolean read;
    private Date date;
}
