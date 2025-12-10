package co.edu.udistrital.mdp.back.dto;

import lombok.Data;

@Data
public class UserDTO extends PersonDTO {
    private Long id;
    private Integer loyaltypoints;
}