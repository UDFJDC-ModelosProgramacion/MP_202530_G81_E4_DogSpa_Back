package co.edu.udistrital.mdp.back.dto;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ServiceDetailDTO extends ServiceDTO {
    private List<ReviewDTO> reviews = new ArrayList<>();
    private List<MultimediaDTO> multimedia = new ArrayList<>();
    private List<BranchDTO> branches = new ArrayList<>();

}