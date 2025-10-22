package co.edu.udistrital.mdp.back.dto;
import java.util.List;
import java.util.ArrayList;

public class BranchDetailDTO extends BranchDTO {
    private List<ServiceDTO> reviews = new ArrayList<>();
	private List<MultimediaDTO> authors = new ArrayList<>();
    
}
