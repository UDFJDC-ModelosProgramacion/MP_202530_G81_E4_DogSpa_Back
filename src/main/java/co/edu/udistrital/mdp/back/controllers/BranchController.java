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
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.BranchService;

@RestController
@RequestMapping("/branches")
public class BranchController {

    @Autowired
    private BranchService branchService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<BranchDTO> findAll() {
        List<BranchEntity> branches = branchService.getBranches();
        return modelMapper.map(branches, new TypeToken<List<BranchDTO>>() {}.getType());
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public BranchDTO findOne(@PathVariable("id") Long id) throws EntityNotFoundException {
        BranchEntity branch = branchService.getBranch(id);
        return modelMapper.map(branch, BranchDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public BranchDTO create(@RequestBody BranchDTO branchDTO) throws IllegalOperationException {
        BranchEntity branch = modelMapper.map(branchDTO, BranchEntity.class);
        BranchEntity newBranch = branchService.createBranch(branch);
        return modelMapper.map(newBranch, BranchDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public BranchDTO update(@PathVariable("id") Long id, @RequestBody BranchDTO branchDTO)
            throws EntityNotFoundException, IllegalOperationException {
        BranchEntity branch = modelMapper.map(branchDTO, BranchEntity.class);
        BranchEntity updatedBranch = branchService.updateBranch(id, branch);
        return modelMapper.map(updatedBranch, BranchDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) throws EntityNotFoundException {
        branchService.deleteBranch(id);
    }

}
