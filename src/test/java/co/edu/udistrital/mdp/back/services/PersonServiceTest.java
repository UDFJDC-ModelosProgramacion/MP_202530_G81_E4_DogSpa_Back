package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.PersonEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(PersonService.class)
class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();

        PersonEntity p = new PersonEntity();
        p.setName("Base");
        p.setLastname("User");
        p.setEmail("base@user.com");
        p.setTelephone("3000000000");
        p.setAddress("Calle Base");
        p.setPassword("base123");
        personRepository.save(p);
    }

    @Test
    void createPerson_valid_ok() throws IllegalOperationException {
        PersonEntity person = new PersonEntity();
        person.setName("Nuevo");
        person.setLastname("Registro");
        person.setEmail("nuevo@user.com");
        person.setTelephone("3011111111");
        person.setAddress("Calle nueva");
        person.setPassword("clave123");

        PersonEntity result = personService.createPerson(person);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Nuevo", result.getName());
    }

    @Test
    void createPerson_emptyName_exception() {
        PersonEntity person = new PersonEntity();
        person.setName("");
        person.setLastname("Apellido");
        person.setEmail("correo@user.com");
        person.setTelephone("3022222222");
        person.setAddress("Calle X");
        person.setPassword("pass123");

        assertThrows(IllegalOperationException.class, () -> {
            personService.createPerson(person);
        });
    }

    @Test
    void getPerson_valid_ok() throws EntityNotFoundException {
        PersonEntity base = personRepository.findAll().get(0);
        PersonEntity found = personService.getPerson(base.getId());
        assertEquals(base.getEmail(), found.getEmail());
    }

    @Test
    void getPerson_notFound_exception() {
        assertThrows(EntityNotFoundException.class, () -> {
            personService.getPerson(9999L);
        });
    }

    @Test
    void updatePerson_valid_ok() throws IllegalOperationException, EntityNotFoundException {
        PersonEntity base = personRepository.findAll().get(0);
        PersonEntity updated = new PersonEntity();
        updated.setName("Modificado");
        updated.setLastname(base.getLastname());
        updated.setEmail(base.getEmail());
        updated.setTelephone(base.getTelephone());
        updated.setAddress(base.getAddress());
        updated.setPassword(base.getPassword());

        PersonEntity res = personService.updatePerson(base.getId(), updated);
        assertEquals("Modificado", res.getName());
    }

    @Test
    void deletePerson_valid_ok() throws EntityNotFoundException {
        PersonEntity base = personRepository.findAll().get(0);
        personService.deletePerson(base.getId());
        assertFalse(personRepository.findById(base.getId()).isPresent());
    }

    @Test
    void deletePerson_notFound_exception() {
        assertThrows(EntityNotFoundException.class, () -> {
            personService.deletePerson(4444L);
        });
    }
}

