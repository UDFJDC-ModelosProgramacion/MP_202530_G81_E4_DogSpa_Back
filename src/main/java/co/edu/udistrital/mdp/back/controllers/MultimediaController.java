package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.MultimediaDTO;
import co.edu.udistrital.mdp.back.entities.MultimediaEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.MultimediaService;

@RestController
@RequestMapping("/multimedias")
public class MultimediaController {

    private final MultimediaService multimediaService;
    private final ModelMapper modelMapper;

    public MultimediaController(MultimediaService multimediaService, ModelMapper modelMapper) {
        this.multimediaService = multimediaService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<MultimediaDTO> findAll() {
        List<MultimediaEntity> multimedias = multimediaService.getMultimedias();
        return modelMapper.map(multimedias, new TypeToken<List<MultimediaDTO>>() {}.getType());
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public MultimediaDTO findOne(@PathVariable("id") Long id) throws EntityNotFoundException {
        MultimediaEntity multimedia = multimediaService.getMultimedia(id);
        return modelMapper.map(multimedia, MultimediaDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public MultimediaDTO create(@RequestBody MultimediaDTO multimediaDTO)
            throws IllegalOperationException, EntityNotFoundException {
        MultimediaEntity multimedia = modelMapper.map(multimediaDTO, MultimediaEntity.class);
        MultimediaEntity newMultimedia = multimediaService.createMultimedia(multimedia);
        return modelMapper.map(newMultimedia, MultimediaDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public MultimediaDTO update(@PathVariable("id") Long id, @RequestBody MultimediaDTO multimediaDTO)
            throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity multimedia = modelMapper.map(multimediaDTO, MultimediaEntity.class);
        MultimediaEntity updatedMultimedia = multimediaService.updateMultimedia(id, multimedia);
        return modelMapper.map(updatedMultimedia, MultimediaDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) throws EntityNotFoundException, IllegalOperationException {
        multimediaService.deleteMultimedia(id);
    }
}