package co.edu.udistrital.mdp.back.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.entities.PersonEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.PersonService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<PersonEntity> findAll() {
        return personService.getPersons();
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public PersonEntity findOne(@PathVariable("id") Long id) {
        return personService.getPerson(id);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public PersonEntity create(@RequestBody PersonEntity person) throws IllegalOperationException {
        return personService.createPerson(person);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public PersonEntity update(@PathVariable("id") Long id, @RequestBody PersonEntity person)
            throws IllegalOperationException {
        return personService.updatePerson(id, person);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        personService.deletePerson(id);
    }
}
