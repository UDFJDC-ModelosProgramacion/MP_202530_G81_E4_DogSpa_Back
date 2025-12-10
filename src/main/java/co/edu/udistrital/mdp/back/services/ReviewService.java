package co.edu.udistrital.mdp.back.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.ReviewEntity;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.ReviewRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReviewService {

    private static final String REVIEW_NOT_FOUND_MESSAGE = "The review was not found.";

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public ReviewEntity createReview(ReviewEntity review) throws IllegalOperationException {
        log.info("Starting review creation process with date = {}", review.getReviewDate());
        validateReview(review);

        Long serviceId = review.getService().getId();

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalOperationException("The service does not exist."));

        review.setService(service);

        ReviewEntity savedReview = reviewRepository.save(review);
        log.info("Review creation process finished with id = {}", savedReview.getId());
        return savedReview;
    }


    public List<ReviewEntity> getReviews() {
        log.info("Starting process to retrieve all reviews");
        List<ReviewEntity> reviews = reviewRepository.findAll();
        log.info("Finished retrieving reviews: {} found", reviews.size());
        return reviews;
    }

    public ReviewEntity getReview(Long id) throws EntityNotFoundException {
        log.info("Starting process to find review with id = {}", id);
        Optional<ReviewEntity> review = reviewRepository.findById(id);

        if (review.isEmpty()) {
            throw new EntityNotFoundException(REVIEW_NOT_FOUND_MESSAGE);
        }

        log.info("Finished finding review with id = {}", id);
        return review.get();
    }

    @Transactional
    public ReviewEntity updateReview(Long id, ReviewEntity review)
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting review update process with id = {}", id);

        ReviewEntity existing = getReview(id);
        validateReview(review);

        existing.setRating(review.getRating());
        existing.setComments(review.getComments());
        existing.setReviewDate(review.getReviewDate());

        ReviewEntity updatedReview = reviewRepository.save(existing);
        log.info("Review update process finished with id = {}", id);
        return updatedReview;
    }

    @Transactional
    public void deleteReview(Long id) throws EntityNotFoundException {
        log.info("Starting review deletion process with id = {}", id);
        ReviewEntity review = getReview(id);
        reviewRepository.delete(review);
        log.info("Review deletion process finished with id = {}", id);
    }

    private void validateReview(ReviewEntity review) throws IllegalOperationException {
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalOperationException("The rating must be between 1 and 5.");
        }

        if (review.getComments() == null || review.getComments().trim().isEmpty()) {
            throw new IllegalOperationException("The comment cannot be empty.");
        }

        if (review.getReviewDate() == null) {
            throw new IllegalOperationException("The review date is required.");
        }
    }
    public List<ReviewEntity> getReviewsByService(Long serviceId) {
        return reviewRepository.findByServiceId(serviceId);
    }

}
