package co.edu.udistrital.mdp.back.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class UserDetailDTO extends UserDTO {
    private List<ReservationDTO> reservations = new ArrayList<>();
    private List<NotificationDTO> notifications = new ArrayList<>();
    private List<OrderDTO> orders = new ArrayList<>();
}