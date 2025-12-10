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
@RequestMapping("/services")
public class ServiceController {
    
    private final ServiceService serviceService;
    private final ModelMapper modelMapper;
    private final co.edu.udistrital.mdp.back.services.ReviewService reviewService;

    public ServiceController(ServiceService serviceService, ModelMapper modelMapper, co.edu.udistrital.mdp.back.services.ReviewService reviewService) {
        this.serviceService = serviceService;
        this.modelMapper = modelMapper;
        this.reviewService = reviewService;
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

    @GetMapping("/{id}/reviews")
    @ResponseStatus(code = org.springframework.http.HttpStatus.OK)
    public java.util.List<co.edu.udistrital.mdp.back.dto.ReviewDTO> getReviewsForService(@PathVariable Long id) {
        java.util.List<co.edu.udistrital.mdp.back.entities.ReviewEntity> reviews = reviewService.getReviewsByService(id);
        java.lang.reflect.Type listType = new org.modelmapper.TypeToken<java.util.List<co.edu.udistrital.mdp.back.dto.ReviewDTO>>() {}.getType();
        java.util.List<co.edu.udistrital.mdp.back.dto.ReviewDTO> dtos = modelMapper.map(reviews, listType);
        for (int i = 0; i < reviews.size(); i++) {
            var re = reviews.get(i);
            var rd = dtos.get(i);
            if (re.getAuthor() != null) {
                rd.setAuthorId(re.getAuthor().getId());
                rd.setAuthorName(re.getAuthor().getName());
            }
        }
        return dtos;
    }

    @PostMapping("/{id}/reviews")
    @ResponseStatus(code = org.springframework.http.HttpStatus.CREATED)
    public co.edu.udistrital.mdp.back.dto.ReviewDTO createReviewForService(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody co.edu.udistrital.mdp.back.dto.ReviewDTO reviewDTO) throws co.edu.udistrital.mdp.back.exceptions.IllegalOperationException, co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException {
        // Map fields manually and ensure service exists
        co.edu.udistrital.mdp.back.entities.ReviewEntity review = new co.edu.udistrital.mdp.back.entities.ReviewEntity();
        review.setRating(reviewDTO.getRating());
        review.setComments(reviewDTO.getComments());
        review.setReviewDate(reviewDTO.getReviewDate());

        co.edu.udistrital.mdp.back.entities.ServiceEntity service = serviceService.getServiceById(id);
        review.setService(service);

        // set author if provided in DTO
        if (reviewDTO.getAuthorId() != null) {
            co.edu.udistrital.mdp.back.entities.UserEntity u = new co.edu.udistrital.mdp.back.entities.UserEntity();
            u.setId(reviewDTO.getAuthorId());
            u.setName(reviewDTO.getAuthorName());
            review.setAuthor(u);
        }

        co.edu.udistrital.mdp.back.entities.ReviewEntity created = reviewService.createReview(review);
        co.edu.udistrital.mdp.back.dto.ReviewDTO out = modelMapper.map(created, co.edu.udistrital.mdp.back.dto.ReviewDTO.class);
        if (created.getAuthor() != null) {
            out.setAuthorId(created.getAuthor().getId());
            out.setAuthorName(created.getAuthor().getName());
        }
        return out;
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
