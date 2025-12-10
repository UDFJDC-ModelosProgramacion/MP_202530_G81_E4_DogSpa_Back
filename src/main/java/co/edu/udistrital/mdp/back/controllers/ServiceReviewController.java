package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.ReviewDTO;
import co.edu.udistrital.mdp.back.entities.ReviewEntity;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.ReviewService;

@RestController
@RequestMapping("/services/{serviceId}/reviews")
public class ServiceReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReviewDTO> getReviewsByService(@PathVariable Long serviceId) {
        List<ReviewEntity> reviews = reviewService.getReviewsByService(serviceId);
        return modelMapper.map(reviews, new TypeToken<List<ReviewDTO>>() {}.getType());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDTO createReviewForService(
            @PathVariable Long serviceId,
            @RequestBody ReviewDTO dto
    ) throws IllegalOperationException {

        ReviewEntity entity = modelMapper.map(dto, ReviewEntity.class);

        // Asegurar que el servicio no sea null
        if (entity.getService() == null) {
            entity.setService(new ServiceEntity());
        }

        entity.getService().setId(serviceId);

        ReviewEntity created = reviewService.createReview(entity);

        return modelMapper.map(created, ReviewDTO.class);
    }
}
