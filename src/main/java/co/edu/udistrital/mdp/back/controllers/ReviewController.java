package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.ReviewDTO;
import co.edu.udistrital.mdp.back.entities.UserEntity;
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
        List<ReviewDTO> dtos = modelMapper.map(reviews, new TypeToken<List<ReviewDTO>>() {}.getType());
        for (int i = 0; i < reviews.size(); i++) {
            ReviewEntity re = reviews.get(i);
            ReviewDTO rd = dtos.get(i);
            if (re.getAuthor() != null) {
                rd.setAuthorId(re.getAuthor().getId());
                rd.setAuthorName(re.getAuthor().getName());
            }
        }
        return dtos;
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ReviewDTO findOne(@PathVariable("id") Long id) throws EntityNotFoundException {
        ReviewEntity review = reviewService.getReview(id);
        ReviewDTO dto = modelMapper.map(review, ReviewDTO.class);
        if (review.getAuthor() != null) {
            dto.setAuthorId(review.getAuthor().getId());
            dto.setAuthorName(review.getAuthor().getName());
        }
        return dto;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ReviewDTO create(@RequestBody ReviewDTO reviewDTO) throws IllegalOperationException {
        ReviewEntity review = modelMapper.map(reviewDTO, ReviewEntity.class);
        if (reviewDTO.getAuthorId() != null) {
            UserEntity u = new UserEntity();
            u.setId(reviewDTO.getAuthorId());
            u.setName(reviewDTO.getAuthorName());
            review.setAuthor(u);
        }
        ReviewEntity newReview = reviewService.createReview(review);
        ReviewDTO dto = modelMapper.map(newReview, ReviewDTO.class);
        if (newReview.getAuthor() != null) {
            dto.setAuthorId(newReview.getAuthor().getId());
            dto.setAuthorName(newReview.getAuthor().getName());
        }
        return dto;
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ReviewDTO update(@PathVariable("id") Long id, @RequestBody ReviewDTO reviewDTO)
            throws EntityNotFoundException, IllegalOperationException {
        ReviewEntity review = modelMapper.map(reviewDTO, ReviewEntity.class);
        if (reviewDTO.getAuthorId() != null) {
            UserEntity u = new UserEntity();
            u.setId(reviewDTO.getAuthorId());
            u.setName(reviewDTO.getAuthorName());
            review.setAuthor(u);
        }
        ReviewEntity updatedReview = reviewService.updateReview(id, review);
        ReviewDTO dto = modelMapper.map(updatedReview, ReviewDTO.class);
        if (updatedReview.getAuthor() != null) {
            dto.setAuthorId(updatedReview.getAuthor().getId());
            dto.setAuthorName(updatedReview.getAuthor().getName());
        }
        return dto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id, @RequestHeader(value = "X-User-Id", required = false) Long userId) throws EntityNotFoundException {
        // If a userId header is provided, enforce that only the author can delete their review
        if (userId != null) {
            ReviewEntity review = reviewService.getReview(id);
            if (review.getAuthor() == null || !userId.equals(review.getAuthor().getId())) {
                throw new IllegalOperationException("Solo el autor puede eliminar su reseña.");
            }
        }
        reviewService.deleteReview(id);
    }
}