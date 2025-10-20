package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.PersonEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private static final String PERSON_NOT_FOUND = "Persona no encontrada";
    private static final String PERSON_NAME_REQUIRED = "El nombre no puede estar vacío";
    private static final String PERSON_EMAIL_INVALID = "El correo es inválido";

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public PersonEntity createPerson(PersonEntity person) throws IllegalOperationException {
        if (person.getName() == null || person.getName().isBlank()) {
            throw new IllegalOperationException(PERSON_NAME_REQUIRED);
        }
        if (person.getEmail() == null || !person.getEmail().contains("@")) {
            throw new IllegalOperationException(PERSON_EMAIL_INVALID);
        }
        return personRepository.save(person);
    }

    public PersonEntity getPerson(Long id) {
        Optional<PersonEntity> person = personRepository.findById(id);
        if (person.isEmpty()) {
            throw new EntityNotFoundException(PERSON_NOT_FOUND);
        }
        return person.get();
    }

    public List<PersonEntity> getPersons() {
        return personRepository.findAll();
    }

    public PersonEntity updatePerson(Long id, PersonEntity person)
            throws IllegalOperationException {

        Optional<PersonEntity> existing = personRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException(PERSON_NOT_FOUND);
        }
        if (person.getName() == null || person.getName().isBlank()) {
            throw new IllegalOperationException(PERSON_NAME_REQUIRED);
        }

        person.setId(id);
        return personRepository.save(person);
    }

    public void deletePerson(Long id) {
        Optional<PersonEntity> existing = personRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException(PERSON_NOT_FOUND);
        }
        personRepository.deleteById(id);
    }
}
