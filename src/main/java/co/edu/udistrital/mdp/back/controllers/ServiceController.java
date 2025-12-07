package co.edu.udistrital.mdp.back.controllers;
import java.lang.reflect.Type;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.udistrital.mdp.back.dto.ServiceDTO;
import co.edu.udistrital.mdp.back.dto.ServiceDetailDTO;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.services.ServiceService;


@RestController
@RequestMapping("/api/services")
public class ServiceController {
    
    private final ServiceService serviceService;
    private final ModelMapper modelMapper;

    public ServiceController(ServiceService serviceService, ModelMapper modelMapper) {
        this.serviceService = serviceService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<ServiceDetailDTO> findAll() {
        List<ServiceEntity> entities = serviceService.getAllServices();
        Type listType = new TypeToken<List<ServiceDetailDTO>>() {}.getType();
        return modelMapper.map(entities, listType);
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ServiceDetailDTO findOne(@PathVariable Long id) throws EntityNotFoundException {
        ServiceEntity entity = serviceService.getServiceById(id);
        return modelMapper.map(entity, ServiceDetailDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ServiceDTO create(@RequestBody ServiceDTO dto) {
        ServiceEntity entity = modelMapper.map(dto, ServiceEntity.class);
        ServiceEntity created = serviceService.save(entity);
        return modelMapper.map(created, ServiceDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ServiceDTO update(@PathVariable Long id, @RequestBody ServiceDTO dto)
        throws EntityNotFoundException {
        ServiceEntity entity = modelMapper.map(dto, ServiceEntity.class);
        ServiceEntity updated = serviceService.updateService(id, entity);
        return modelMapper.map(updated, ServiceDTO.class);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws EntityNotFoundException {
        serviceService.delete(id);
    }
}
