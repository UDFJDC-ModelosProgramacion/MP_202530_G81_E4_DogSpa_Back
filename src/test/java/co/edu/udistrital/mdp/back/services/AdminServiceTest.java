package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.AdminEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AdminService.class)
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        adminRepository.deleteAll();

        AdminEntity a = new AdminEntity();
        a.setName("BaseAdmin");
        a.setLastname("Gomez");
        a.setEmail("admin@dogspa.com");
        a.setTelephone("3040000000");
        a.setAddress("Calle Base");
        a.setPassword("admin123");
        adminRepository.save(a);
    }

    @Test
    void testCreateAdmin_valid_ok() throws IllegalOperationException {
        AdminEntity admin = new AdminEntity();
        admin.setName("Nuevo");
        admin.setLastname("Administrador");
        admin.setEmail("nuevo@dogspa.com");
        admin.setTelephone("3051111111");
        admin.setAddress("Calle 50");
        admin.setPassword("pass456");

        AdminEntity result = adminService.createAdmin(admin);
        assertNotNull(result);
        assertEquals("Nuevo", result.getName());
    }

    @Test
    void testCreateAdmin_emptyName_exception() {
        AdminEntity admin = new AdminEntity();
        admin.setName("");
        admin.setLastname("Apellido");
        admin.setEmail("correo@dogspa.com");
        admin.setTelephone("3052222222");
        admin.setAddress("Calle 20");
        admin.setPassword("clave123");

        assertThrows(IllegalOperationException.class, () -> {
            adminService.createAdmin(admin);
        });
    }

    @Test
    void testGetAdmin_valid_ok() throws EntityNotFoundException {
        AdminEntity base = adminRepository.findAll().get(0);
        AdminEntity found = adminService.getAdmin(base.getId());
        assertEquals(base.getEmail(), found.getEmail());
    }

    @Test
    void testGetAdmin_notFound_exception() {
        assertThrows(EntityNotFoundException.class, () -> {
            adminService.getAdmin(9999L);
        });
    }
}
