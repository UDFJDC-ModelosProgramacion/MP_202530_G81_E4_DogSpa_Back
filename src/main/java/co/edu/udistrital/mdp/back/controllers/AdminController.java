package co.edu.udistrital.mdp.back.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import co.edu.udistrital.mdp.back.dto.UserDTO;
import co.edu.udistrital.mdp.back.entities.AdminEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.AdminService;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;
    private final ModelMapper modelMapper;

    public AdminController(AdminService adminService, ModelMapper modelMapper) {
        this.adminService = adminService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public UserDTO findOne(@PathVariable("id") Long id) {
        AdminEntity admin = adminService.getAdmin(id);
        return modelMapper.map(admin, UserDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDTO create(@RequestBody UserDTO adminDTO) throws IllegalOperationException {
        AdminEntity admin = modelMapper.map(adminDTO, AdminEntity.class);
        AdminEntity newAdmin = adminService.createAdmin(admin);
        return modelMapper.map(newAdmin, UserDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public UserDTO update(@PathVariable("id") Long id, @RequestBody UserDTO adminDTO)
            throws IllegalOperationException {
        AdminEntity admin = modelMapper.map(adminDTO, AdminEntity.class);
        AdminEntity updatedAdmin = adminService.updateAdmin(id, admin);
        return modelMapper.map(updatedAdmin, UserDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        adminService.deleteAdmin(id);
    }
}
