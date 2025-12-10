package co.edu.udistrital.mdp.back.controllers;

import java.lang.reflect.Type;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import co.edu.udistrital.mdp.back.dto.UserDTO;
import co.edu.udistrital.mdp.back.entities.PersonEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.PersonService;

@RestController
@RequestMapping("/persons")
public class PersonController {

    private final PersonService personService;
    private final ModelMapper modelMapper;

    public PersonController(PersonService personService, ModelMapper modelMapper) {
        this.personService = personService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<UserDTO> findAll() {
        List<PersonEntity> persons = personService.getPersons();
        Type listType = new TypeToken<List<UserDTO>>() {}.getType();
        return modelMapper.map(persons, listType);
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public UserDTO findOne(@PathVariable("id") Long id) {
        PersonEntity person = personService.getPerson(id);
        return modelMapper.map(person, UserDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDTO create(@RequestBody UserDTO personDTO) throws IllegalOperationException {
        PersonEntity person = modelMapper.map(personDTO, PersonEntity.class);
        PersonEntity newPerson = personService.createPerson(person);
        return modelMapper.map(newPerson, UserDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public UserDTO update(@PathVariable("id") Long id, @RequestBody UserDTO personDTO)
            throws IllegalOperationException {
        PersonEntity person = modelMapper.map(personDTO, PersonEntity.class);
        PersonEntity updatedPerson = personService.updatePerson(id, person);
        return modelMapper.map(updatedPerson, UserDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        personService.deletePerson(id);
    }
}
