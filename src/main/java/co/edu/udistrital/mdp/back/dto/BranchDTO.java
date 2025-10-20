package co.edu.udistrital.mdp.back.dto;
import lombok.Data;

@Data
public class BranchDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String zone; 
}
