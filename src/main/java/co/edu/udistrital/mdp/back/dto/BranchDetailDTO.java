package co.edu.udistrital.mdp.back.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BranchDetailDTO extends BranchDTO {
    private List<ServiceDTO> services = new ArrayList<>();
    private List<MultimediaDTO> multimedia = new ArrayList<>();
}