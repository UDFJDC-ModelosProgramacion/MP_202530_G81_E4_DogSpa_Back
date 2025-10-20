package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.AdminEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    private AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public AdminEntity createAdmin(AdminEntity admin) throws IllegalOperationException {
        if (admin.getName() == null || admin.getName().isBlank()) {
            throw new IllegalOperationException("El nombre del administrador es obligatorio");
        }
        return adminRepository.save(admin);
    }

    public AdminEntity getAdmin(Long id) {
        Optional<AdminEntity> admin = adminRepository.findById(id);
        if (admin.isEmpty()) {
            throw new EntityNotFoundException("Administrador no encontrado");
        }
        return admin.get();
    }

    public AdminEntity updateAdmin(Long id, AdminEntity admin)
            throws IllegalOperationException {

        Optional<AdminEntity> existing = adminRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException("Administrador no encontrado");
        }

        if (admin.getName() == null || admin.getName().isBlank()) {
            throw new IllegalOperationException("El nombre del administrador es obligatorio");
        }

        admin.setId(id);
        return adminRepository.save(admin);
    }

    public void deleteAdmin(Long id) {
        Optional<AdminEntity> existing = adminRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException("Administrador no encontrado");
        }
        adminRepository.deleteById(id);
    }
}
