package co.edu.udistrital.mdp.back.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.entities.AdminEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.AdminService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public AdminEntity findOne(@PathVariable("id") Long id) {
        return adminService.getAdmin(id);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public AdminEntity create(@RequestBody AdminEntity admin) throws IllegalOperationException {
        return adminService.createAdmin(admin);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public AdminEntity update(@PathVariable("id") Long id, @RequestBody AdminEntity admin)
            throws IllegalOperationException {
        return adminService.updateAdmin(id, admin);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        adminService.deleteAdmin(id);
    }
}
