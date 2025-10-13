package co.edu.udistrital.mdp.back.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import co.edu.udistrital.mdp.back.entities.ReviewEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.ReviewRepository;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import(ReviewService.class)
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private PodamFactory factory = new PodamFactoryImpl();

    private List<ReviewEntity> reviewList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from ReviewEntity").executeUpdate();
    }

    private void insertData() {
        for (int i = 0; i < 3; i++) {
            ReviewEntity entity = factory.manufacturePojo(ReviewEntity.class);
            entity.setRating((i % 5) + 1); // rating between 1 and 5
            entity.setComments("Comment " + i);
            entity.setReviewDate(LocalDate.now().minusDays(i));
            entityManager.persist(entity);
            reviewList.add(entity);
        }
    }

    @Test
    void testCreateReview() throws IllegalOperationException {
        ReviewEntity newEntity = new ReviewEntity();
        newEntity.setRating(4);
        newEntity.setComments("Excellent service");
        newEntity.setReviewDate(LocalDate.now());

        ReviewEntity result = reviewService.createReview(newEntity);

        assertNotNull(result);
        ReviewEntity entity = entityManager.find(ReviewEntity.class, result.getId());
        assertEquals(newEntity.getRating(), entity.getRating());
        assertEquals(newEntity.getComments(), entity.getComments());
        assertEquals(newEntity.getReviewDate(), entity.getReviewDate());
    }

    @Test
    void testCreateReviewInvalidRating() {
        ReviewEntity newEntity = new ReviewEntity();
        newEntity.setRating(0);
        newEntity.setComments("Very bad");
        newEntity.setReviewDate(LocalDate.now());

        assertThrows(IllegalOperationException.class, () -> reviewService.createReview(newEntity));
    }

    @Test
    void testCreateReviewNoComments() {
        ReviewEntity newEntity = new ReviewEntity();
        newEntity.setRating(4);
        newEntity.setComments("   "); // empty
        newEntity.setReviewDate(LocalDate.now());

        assertThrows(IllegalOperationException.class, () -> reviewService.createReview(newEntity));
    }

    @Test
    void testCreateReviewNoDate() {
        ReviewEntity newEntity = new ReviewEntity();
        newEntity.setRating(5);
        newEntity.setComments("No date");

        assertThrows(IllegalOperationException.class, () -> reviewService.createReview(newEntity));
    }

    @Test
    void testGetReviews() {
        List<ReviewEntity> list = reviewService.getReviews();
        assertEquals(reviewList.size(), list.size());
        assertTrue(list.stream().anyMatch(r -> r.getComments().contains("Comment")));
    }

    @Test
    void testGetReview() throws EntityNotFoundException {
        ReviewEntity entity = reviewList.get(0);
        ReviewEntity result = reviewService.getReview(entity.getId());
        assertNotNull(result);
        assertEquals(entity.getComments(), result.getComments());
    }

    @Test
    void testGetInvalidReview() {
        assertThrows(EntityNotFoundException.class, () -> reviewService.getReview(999L));
    }

    @Test
    void testUpdateReview() throws EntityNotFoundException, IllegalOperationException {
        ReviewEntity entity = reviewList.get(0);
        ReviewEntity update = new ReviewEntity();
        update.setRating(5);
        update.setComments("Updated");
        update.setReviewDate(LocalDate.now());

        ReviewEntity updated = reviewService.updateReview(entity.getId(), update);
        assertEquals(5, updated.getRating());
        assertEquals("Updated", updated.getComments());
    }

    @Test
    void testUpdateInvalidReview() {
        ReviewEntity update = new ReviewEntity();
        update.setRating(3);
        update.setComments("Failed attempt");
        update.setReviewDate(LocalDate.now());

        assertThrows(EntityNotFoundException.class, () -> reviewService.updateReview(999L, update));
    }

    @Test
    void testDeleteReview() throws EntityNotFoundException {
        ReviewEntity entity = reviewList.get(0);
        reviewService.deleteReview(entity.getId());
        ReviewEntity deleted = entityManager.find(ReviewEntity.class, entity.getId());
        assertNull(deleted);
    }

    @Test
    void testDeleteInvalidReview() {
        assertThrows(EntityNotFoundException.class, () -> reviewService.deleteReview(999L));
    }
}
