/*
MIT License

Copyright (c) 2025 Universidad Distrital
*/

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

    /**
     * Configuración inicial de la prueba.
     */
    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    /**
     * Limpia las tablas implicadas en la prueba.
     */
    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from ReviewEntity").executeUpdate();
    }

    /**
     * Inserta datos iniciales para el correcto funcionamiento de las pruebas.
     */
    private void insertData() {
        for (int i = 0; i < 3; i++) {
            ReviewEntity entity = factory.manufacturePojo(ReviewEntity.class);
            entity.setRating((i % 5) + 1); // rating entre 1 y 5
            entity.setComments("Comentario " + i);
            entity.setReviewDate(LocalDate.now().minusDays(i));
            entityManager.persist(entity);
            reviewList.add(entity);
        }
    }

    /**
     * Prueba para crear una reseña válida.
     */
    @Test
    void testCreateReview() throws IllegalOperationException {
        ReviewEntity newEntity = new ReviewEntity();
        newEntity.setRating(4);
        newEntity.setComments("Excelente servicio");
        newEntity.setReviewDate(LocalDate.now());

        ReviewEntity result = reviewService.createReview(newEntity);

        assertNotNull(result);
        ReviewEntity entity = entityManager.find(ReviewEntity.class, result.getId());
        assertEquals(newEntity.getRating(), entity.getRating());
        assertEquals(newEntity.getComments(), entity.getComments());
        assertEquals(newEntity.getReviewDate(), entity.getReviewDate());
    }

    /**
     * Prueba para crear una reseña con calificación inválida.
     */
    @Test
    void testCreateReviewInvalidRating() {
        ReviewEntity newEntity = new ReviewEntity();
        newEntity.setRating(0);
        newEntity.setComments("Muy malo");
        newEntity.setReviewDate(LocalDate.now());

        assertThrows(IllegalOperationException.class, () -> reviewService.createReview(newEntity));
    }

    /**
     * Prueba para crear una reseña sin comentarios.
     */
    @Test
    void testCreateReviewNoComments() {
        ReviewEntity newEntity = new ReviewEntity();
        newEntity.setRating(4);
        newEntity.setComments("   "); // vacío
        newEntity.setReviewDate(LocalDate.now());

        assertThrows(IllegalOperationException.class, () -> reviewService.createReview(newEntity));
    }

    /**
     * Prueba para crear una reseña sin fecha.
     */
    @Test
    void testCreateReviewNoDate() {
        ReviewEntity newEntity = new ReviewEntity();
        newEntity.setRating(5);
        newEntity.setComments("Sin fecha");

        assertThrows(IllegalOperationException.class, () -> reviewService.createReview(newEntity));
    }

    /**
     * Prueba para obtener todas las reseñas.
     */
    @Test
    void testGetReviews() {
        List<ReviewEntity> list = reviewService.getReviews();
        assertEquals(reviewList.size(), list.size());
        assertTrue(list.stream().anyMatch(r -> r.getComments().contains("Comentario")));
    }

    /**
     * Prueba para obtener una reseña por ID.
     */
    @Test
    void testGetReview() throws EntityNotFoundException {
        ReviewEntity entity = reviewList.get(0);
        ReviewEntity result = reviewService.getReview(entity.getId());
        assertNotNull(result);
        assertEquals(entity.getComments(), result.getComments());
    }

    /**
     * Prueba para obtener una reseña inexistente.
     */
    @Test
    void testGetInvalidReview() {
        assertThrows(EntityNotFoundException.class, () -> reviewService.getReview(999L));
    }

    /**
     * Prueba para actualizar una reseña existente.
     */
    @Test
    void testUpdateReview() throws EntityNotFoundException, IllegalOperationException {
        ReviewEntity entity = reviewList.get(0);
        ReviewEntity update = new ReviewEntity();
        update.setRating(5);
        update.setComments("Actualizado");
        update.setReviewDate(LocalDate.now());

        ReviewEntity updated = reviewService.updateReview(entity.getId(), update);
        assertEquals(5, updated.getRating());
        assertEquals("Actualizado", updated.getComments());
    }

    /**
     * Prueba para actualizar una reseña inexistente.
     */
    @Test
    void testUpdateInvalidReview() {
        ReviewEntity update = new ReviewEntity();
        update.setRating(3);
        update.setComments("Intento fallido");
        update.setReviewDate(LocalDate.now());

        assertThrows(EntityNotFoundException.class, () -> reviewService.updateReview(999L, update));
    }

    /**
     * Prueba para eliminar una reseña existente.
     */
    @Test
    void testDeleteReview() throws EntityNotFoundException {
        ReviewEntity entity = reviewList.get(0);
        reviewService.deleteReview(entity.getId());
        ReviewEntity deleted = entityManager.find(ReviewEntity.class, entity.getId());
        assertNull(deleted);
    }

    /**
     * Prueba para eliminar una reseña inexistente.
     */
    @Test
    void testDeleteInvalidReview() {
        assertThrows(EntityNotFoundException.class, () -> reviewService.deleteReview(999L));
    }
}
