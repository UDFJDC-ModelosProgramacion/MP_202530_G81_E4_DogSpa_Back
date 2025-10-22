package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.BranchDTO;
import co.edu.udistrital.mdp.back.entities.BranchEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.services.ServiceBranchService;

@RestController
@RequestMapping("/api/services")
public class ServiceBranchController {

    @Autowired
    private ServiceBranchService serviceBranchService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/{serviceId}/branches")
    @ResponseStatus(code = HttpStatus.OK)
    public List<BranchDTO> getBranches(@PathVariable Long serviceId) throws EntityNotFoundException {
        List<BranchEntity> branches = serviceBranchService.getBranches(serviceId);
        return modelMapper.map(branches, new TypeToken<List<BranchDTO>>() {}.getType());
    }

    @PostMapping("/{serviceId}/branches/{branchId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void addBranch(@PathVariable Long serviceId, @PathVariable Long branchId)
            throws EntityNotFoundException {
        serviceBranchService.addBranchToService(serviceId, branchId);
    }

    @DeleteMapping("/{serviceId}/branches/{branchId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeBranch(@PathVariable Long serviceId, @PathVariable Long branchId)
            throws EntityNotFoundException {
        serviceBranchService.removeBranchFromService(serviceId, branchId);
    }
}