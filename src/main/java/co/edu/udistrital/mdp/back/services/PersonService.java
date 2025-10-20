package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.PersonEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    public PersonEntity createPerson(PersonEntity person) throws IllegalOperationException {
        if (person.getName() == null || person.getName().isBlank()) {
            throw new IllegalOperationException("El nombre no puede estar vacío");
        }
        if (person.getEmail() == null || !person.getEmail().contains("@")) {
            throw new IllegalOperationException("El correo es inválido");
        }
        return personRepository.save(person);
    }

    public PersonEntity getPerson(Long id) {
        Optional<PersonEntity> person = personRepository.findById(id);
        if (person.isEmpty()) {
            throw new EntityNotFoundException("Persona no encontrada");
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
            throw new EntityNotFoundException("Persona no encontrada");
        }
        if (person.getName() == null || person.getName().isBlank()) {
            throw new IllegalOperationException("El nombre no puede estar vacío");
        }

        person.setId(id);
        return personRepository.save(person);
    }

    public void deletePerson(Long id) {
        Optional<PersonEntity> existing = personRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException("Persona no encontrada");
        }
        personRepository.deleteById(id);
    }
}

