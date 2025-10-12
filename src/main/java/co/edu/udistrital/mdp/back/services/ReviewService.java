package co.edu.udistrital.mdp.back.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.ReviewEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.ReviewRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public ReviewEntity createReview(ReviewEntity review) throws IllegalOperationException {
        log.info("Inicia proceso de creación de reseña con fecha = {}", review.getReviewDate());
        validateReview(review);

        ReviewEntity savedReview = reviewRepository.save(review);
        log.info("Termina proceso de creación de reseña con id = {}", savedReview.getId());
        return savedReview;
    }

    public List<ReviewEntity> getReviews() {
        log.info("Inicia proceso de consulta de todas las reseñas");
        List<ReviewEntity> reviews = reviewRepository.findAll();
        log.info("Termina proceso de consulta: se encontraron {} reseñas", reviews.size());
        return reviews;
    }

    public ReviewEntity getReview(Long id) throws EntityNotFoundException {
        log.info("Inicia proceso de búsqueda de reseña con id = {}", id);
        Optional<ReviewEntity> review = reviewRepository.findById(id);

        if (review.isEmpty()) {
            throw new EntityNotFoundException("La reseña no fue encontrada.");
        }

        log.info("Termina proceso de búsqueda de reseña con id = {}", id);
        return review.get();
    }

    @Transactional
    public ReviewEntity updateReview(Long id, ReviewEntity review)
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Inicia proceso de actualización de reseña con id = {}", id);

        ReviewEntity existing = getReview(id);
        validateReview(review);

        existing.setRating(review.getRating());
        existing.setComments(review.getComments());
        existing.setReviewDate(review.getReviewDate());

        ReviewEntity updatedReview = reviewRepository.save(existing);
        log.info("Termina proceso de actualización de reseña con id = {}", id);
        return updatedReview;
    }

    @Transactional
    public void deleteReview(Long id) throws EntityNotFoundException {
        log.info("Inicia proceso de eliminación de reseña con id = {}", id);
        ReviewEntity review = getReview(id);
        reviewRepository.delete(review);
        log.info("Termina proceso de eliminación de reseña con id = {}", id);
    }

    private void validateReview(ReviewEntity review) throws IllegalOperationException {
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalOperationException("La calificación debe estar entre 1 y 5.");
        }

        if (review.getComments() == null || review.getComments().trim().isEmpty()) {
            throw new IllegalOperationException("El comentario no puede estar vacío.");
        }

        if (review.getReviewDate() == null) {
            throw new IllegalOperationException("La fecha de reseña es obligatoria.");
        }
    }
}
