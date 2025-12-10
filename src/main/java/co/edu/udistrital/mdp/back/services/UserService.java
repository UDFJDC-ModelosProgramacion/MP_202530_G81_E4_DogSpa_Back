package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import co.edu.udistrital.mdp.back.repositories.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Objects;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String INVALID_EMAIL = "Correo inválido";

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public UserService(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    public UserEntity getUserByEmail(String email) {
        Optional<UserEntity> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(USER_NOT_FOUND);
        }
        return user.get();
    }

    public co.edu.udistrital.mdp.back.entities.PersonEntity authenticate(String email, String password) {
        // 1. Try User
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            if (Objects.equals(user.getPassword(), password)) {
                user.setRole("USER");
                return user;
            } else {
                throw new IllegalOperationException("Contraseña incorrecta");
            }
        }

        // 2. Try Admin
        Optional<co.edu.udistrital.mdp.back.entities.AdminEntity> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            co.edu.udistrital.mdp.back.entities.AdminEntity admin = adminOpt.get();
            if (Objects.equals(admin.getPassword(), password)) {
                admin.setRole("ADMIN");
                return admin;
            } else {
                throw new IllegalOperationException("Contraseña incorrecta");
            }
        }

        throw new EntityNotFoundException(USER_NOT_FOUND);
    }

    public UserEntity createUser(UserEntity user) throws IllegalOperationException {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalOperationException(INVALID_EMAIL);
        }
        return userRepository.save(user);
    }

    public UserEntity getUser(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(USER_NOT_FOUND);
        }
        return user.get();
    }

    public UserEntity updateUser(Long id, UserEntity user)
            throws IllegalOperationException {

        Optional<UserEntity> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException(USER_NOT_FOUND);
        }

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalOperationException(INVALID_EMAIL);
        }

        user.setId(id);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        Optional<UserEntity> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException(USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }
}
