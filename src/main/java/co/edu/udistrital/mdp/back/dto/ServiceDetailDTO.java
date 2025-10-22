package co.edu.udistrital.mdp.back.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceDetailDTO extends ServiceDTO {
    private List<ReviewDTO> reviews = new ArrayList<>();
    private List<MultimediaDTO> multimedia = new ArrayList<>();
    private List<BranchDTO> branches = new ArrayList<>();
}