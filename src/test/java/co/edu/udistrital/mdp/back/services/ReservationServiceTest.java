
package co.edu.udistrital.mdp.back.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import co.edu.udistrital.mdp.back.entities.BranchEntity;
import co.edu.udistrital.mdp.back.entities.ReservationEntity;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import(ReservationService.class)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private List<ReservationEntity> reservationList = new ArrayList<>();

    private BranchEntity branch;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from ReservationEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from BranchEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from UserEntity").executeUpdate();
    }

    private void insertData() {
        branch = new BranchEntity();
        branch.setName("Sucursal Central");
        branch.setAddress("Calle 100 #50-25");
        branch.setPhone("3105555555");
        branch.setZone("Norte");
        entityManager.persist(branch);

        user = new UserEntity();
        user.setName("Juan Pérez");
        user.setEmail("juan.perez@example.com");
        user.setPassword("1234");
        entityManager.persist(user);

        for (int i = 0; i < 3; i++) {
            ReservationEntity entity = new ReservationEntity();
            entity.setReservationDate(LocalDate.now().plusDays(i));
            entity.setStartTime(LocalTime.of(10 + i, 0));
            entity.setEndTime(LocalTime.of(11 + i, 0));
            entity.setReservationStatus("CONFIRMED");
            entity.setBranch(branch);
            entity.setUser(user);
            entityManager.persist(entity);
            reservationList.add(entity);
        }
    }


    @Test
    void testCreateReservation() throws IllegalOperationException {
        ReservationEntity newEntity = new ReservationEntity();
        newEntity.setReservationDate(LocalDate.now().plusDays(1));
        newEntity.setStartTime(LocalTime.of(9, 0));
        newEntity.setEndTime(LocalTime.of(10, 0));
        newEntity.setReservationStatus("PENDING");
        newEntity.setBranch(branch);
        newEntity.setUser(user);

        ReservationEntity result = reservationService.createReservation(newEntity);
        assertNotNull(result);

        ReservationEntity stored = entityManager.find(ReservationEntity.class, result.getId());
        assertEquals(newEntity.getReservationStatus(), stored.getReservationStatus());
        assertEquals(newEntity.getStartTime(), stored.getStartTime());
    }

    @Test
    void testCreateReservationInvalidTime() {
        ReservationEntity newEntity = new ReservationEntity();
        newEntity.setReservationDate(LocalDate.now());
        newEntity.setStartTime(LocalTime.of(12, 0));
        newEntity.setEndTime(LocalTime.of(10, 0));
        newEntity.setReservationStatus("INVALID");
        newEntity.setBranch(branch);
        newEntity.setUser(user);

        assertThrows(IllegalOperationException.class, () -> reservationService.createReservation(newEntity));
    }

    @Test
    void testGetReservations() {
        List<ReservationEntity> list = reservationService.getAllReservations();
        assertEquals(reservationList.size(), list.size());
    }


  @Test
    void testGetReservation() throws EntityNotFoundException {
        ReservationEntity entity = reservationList.get(0);
        ReservationEntity resultEntity = reservationService.getReservationById(entity.getId());
        assertNotNull(resultEntity);
        assertEquals(entity.getReservationStatus(), resultEntity.getReservationStatus());
    }


    @Test
        void testGetInvalidReservation() {
            assertThrows(jakarta.persistence.EntityNotFoundException.class,
                    () -> reservationService.getReservationById(999L));
        }


    @Test
    void testUpdateReservation() throws EntityNotFoundException, IllegalOperationException {
        ReservationEntity entity = reservationList.get(0);
        ReservationEntity update = new ReservationEntity();
        update.setReservationDate(LocalDate.now().plusDays(2));
        update.setStartTime(LocalTime.of(14, 0));
        update.setEndTime(LocalTime.of(15, 0));
        update.setReservationStatus("UPDATED");
        update.setBranch(branch);
        update.setUser(user);

        ReservationEntity updated = reservationService.updateReservation(entity.getId(), update);
        assertEquals("UPDATED", updated.getReservationStatus());
        assertEquals(LocalTime.of(14, 0), updated.getStartTime());
    }

   @Test
        void testUpdateInvalidReservation() {
            ReservationEntity updated = new ReservationEntity();
            updated.setReservationStatus("UPDATED");

            assertThrows(jakarta.persistence.EntityNotFoundException.class,
                    () -> reservationService.updateReservation(999L, updated));
        }



    @Test
    void testUpdateReservationInvalidTime() {
        ReservationEntity entity = reservationList.get(0);
        ReservationEntity update = new ReservationEntity();
        update.setStartTime(LocalTime.of(12, 0));
        update.setEndTime(LocalTime.of(10, 0));
        update.setReservationDate(LocalDate.now());
        update.setReservationStatus("ERROR");
        update.setBranch(branch);
        update.setUser(user);

        assertThrows(IllegalOperationException.class, () -> reservationService.updateReservation(entity.getId(), update));
    }

    @Test
    void testDeleteReservation() throws EntityNotFoundException {
        ReservationEntity entity = reservationList.get(0);
        reservationService.deleteReservation(entity.getId());
        ReservationEntity deleted = entityManager.find(ReservationEntity.class, entity.getId());
        assertNull(deleted);
    }

    @Test
        void testDeleteInvalidReservation() {
            assertThrows(jakarta.persistence.EntityNotFoundException.class,
                    () -> reservationService.deleteReservation(999L));
        }

}
