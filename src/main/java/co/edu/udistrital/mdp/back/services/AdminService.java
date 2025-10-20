package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.AdminEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    private static final String ADMIN_NOT_FOUND = "Administrador no encontrado";
    private static final String ADMIN_NAME_REQUIRED = "El nombre del administrador es obligatorio";

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public AdminEntity createAdmin(AdminEntity admin) throws IllegalOperationException {
        if (admin.getName() == null || admin.getName().isBlank()) {
            throw new IllegalOperationException(ADMIN_NAME_REQUIRED);
        }
        return adminRepository.save(admin);
    }

    public AdminEntity getAdmin(Long id) {
        Optional<AdminEntity> admin = adminRepository.findById(id);
        if (admin.isEmpty()) {
            throw new EntityNotFoundException(ADMIN_NOT_FOUND);
        }
        return admin.get();
    }

    public AdminEntity updateAdmin(Long id, AdminEntity admin)
            throws IllegalOperationException {

        Optional<AdminEntity> existing = adminRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException(ADMIN_NOT_FOUND);
        }

        if (admin.getName() == null || admin.getName().isBlank()) {
            throw new IllegalOperationException(ADMIN_NAME_REQUIRED);
        }

        admin.setId(id);
        return adminRepository.save(admin);
    }

    public void deleteAdmin(Long id) {
        Optional<AdminEntity> existing = adminRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException(ADMIN_NOT_FOUND);
        }
        adminRepository.deleteById(id);
    }
}
