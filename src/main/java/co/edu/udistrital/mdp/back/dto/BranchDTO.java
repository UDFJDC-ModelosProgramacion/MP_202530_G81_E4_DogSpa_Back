package co.edu.udistrital.mdp.back.dto;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BranchDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String zone; 
    private List<MultimediaDTO> multimedia = new ArrayList<>();
}
