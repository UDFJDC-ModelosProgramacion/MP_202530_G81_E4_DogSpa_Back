package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.ServiceDTO;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.services.BranchServiceService;

@RestController
@RequestMapping("/branches")
public class BranchServiceController {

    @Autowired
    private BranchServiceService branchServiceService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/{branchId}/services")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ServiceDTO> getServices(@PathVariable Long branchId) throws EntityNotFoundException {
        List<ServiceEntity> services = branchServiceService.getServices(branchId);
        return modelMapper.map(services, new TypeToken<List<ServiceDTO>>() {}.getType());
    }

    @PostMapping("/{branchId}/services/{serviceId}")
    @ResponseStatus(code = HttpStatus.OK)
    public void addService(@PathVariable Long branchId, @PathVariable Long serviceId)
            throws EntityNotFoundException {
        branchServiceService.addServiceToBranch(branchId, serviceId);
    }

    @DeleteMapping("/{branchId}/services/{serviceId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeService(@PathVariable Long branchId, @PathVariable Long serviceId)
            throws EntityNotFoundException {
        branchServiceService.removeServiceFromBranch(branchId, serviceId);
    }
}