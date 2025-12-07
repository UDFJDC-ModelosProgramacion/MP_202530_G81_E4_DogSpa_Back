package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.ReviewDTO;
import co.edu.udistrital.mdp.back.entities.ReviewEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.ReviewService;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<ReviewDTO> findAll() {
        List<ReviewEntity> reviews = reviewService.getReviews();
        return modelMapper.map(reviews, new TypeToken<List<ReviewDTO>>() {}.getType());
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ReviewDTO findOne(@PathVariable("id") Long id) throws EntityNotFoundException {
        ReviewEntity review = reviewService.getReview(id);
        return modelMapper.map(review, ReviewDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ReviewDTO create(@RequestBody ReviewDTO reviewDTO) throws IllegalOperationException {
        ReviewEntity review = modelMapper.map(reviewDTO, ReviewEntity.class);
        ReviewEntity newReview = reviewService.createReview(review);
        return modelMapper.map(newReview, ReviewDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ReviewDTO update(@PathVariable("id") Long id, @RequestBody ReviewDTO reviewDTO)
            throws EntityNotFoundException, IllegalOperationException {
        ReviewEntity review = modelMapper.map(reviewDTO, ReviewEntity.class);
        ReviewEntity updatedReview = reviewService.updateReview(id, review);
        return modelMapper.map(updatedReview, ReviewDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) throws EntityNotFoundException {
        reviewService.deleteReview(id);
    }
}