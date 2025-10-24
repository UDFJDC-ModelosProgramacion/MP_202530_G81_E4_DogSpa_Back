package co.edu.udistrital.mdp.back.services;

import static org.junit.jupiter.api.Assertions.*;

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
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import(BranchService.class)
class BranchServiceTest {

    @Autowired
    private BranchService branchService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private List<BranchEntity> branchList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from BranchEntity").executeUpdate();
    }

    private void insertData() {
        for (int i = 0; i < 3; i++) {
            BranchEntity entity = new BranchEntity();
            entity.setName("Sucursal " + i);
            entity.setAddress("Calle " + (10 + i));
            entity.setPhone("30012345" + i);
            entity.setZone("Zona " + i);
            entityManager.persist(entity);
            branchList.add(entity);
        }
    }


    @Test
    void testCreateBranch() throws IllegalOperationException {
        BranchEntity newEntity = new BranchEntity();
        newEntity.setName("Nueva Sucursal");
        newEntity.setAddress("Calle 123");
        newEntity.setPhone("3204567890");
        newEntity.setZone("Norte");

        BranchEntity result = branchService.createBranch(newEntity);
        assertNotNull(result);
        
        Long resultId = result.getId();
        BranchEntity stored = entityManager.find(BranchEntity.class, resultId);
        assertEquals(newEntity.getName(), stored.getName());
        assertEquals(newEntity.getAddress(), stored.getAddress());
        assertEquals(newEntity.getPhone(), stored.getPhone());
        assertEquals(newEntity.getZone(), stored.getZone());
    }


    @Test
    void testCreateBranchInvalidName() {
        BranchEntity newEntity = new BranchEntity();
        newEntity.setName(" ");
        newEntity.setAddress("Carrera 45");
        newEntity.setPhone("3001112222");
        newEntity.setZone("Centro");

        assertThrows(IllegalOperationException.class, () -> branchService.createBranch(newEntity));
    }

    @Test
    void testCreateBranchInvalidPhone() {
        BranchEntity newEntity = new BranchEntity();
        newEntity.setName("Sucursal Telefono Malo");
        newEntity.setAddress("Calle 45");
        newEntity.setPhone("12ABCD");
        newEntity.setZone("Occidente");

        assertThrows(IllegalOperationException.class, () -> branchService.createBranch(newEntity));
    }


    @Test
    void testCreateBranchDuplicateId() {
        BranchEntity existing = branchList.get(0);
        Long existingId = existing.getId();
        
        BranchEntity duplicate = new BranchEntity();
        duplicate.setId(existingId);
        duplicate.setName("Sucursal Duplicada");
        duplicate.setAddress("Calle 999");
        duplicate.setPhone("3103334444");
        duplicate.setZone("Sur");

        assertThrows(IllegalOperationException.class, () -> branchService.createBranch(duplicate));
    }


    @Test
    void testGetBranches() {
        List<BranchEntity> list = branchService.getBranches();
        assertEquals(branchList.size(), list.size());
    }

    @Test
    void testGetBranch() throws EntityNotFoundException {
        BranchEntity entity = branchList.get(0);
        Long entityId = entity.getId();
        
        BranchEntity result = branchService.getBranch(entityId);
        assertNotNull(result);
        assertEquals(entity.getName(), result.getName());
    }


    @Test
    void testGetInvalidBranch() {
        assertThrows(EntityNotFoundException.class, () -> branchService.getBranch(999L));
    }

    @Test
    void testUpdateBranch() throws EntityNotFoundException, IllegalOperationException {
        BranchEntity entity = branchList.get(0);
        Long entityId = entity.getId();
        
        BranchEntity update = new BranchEntity();
        update.setName("Sucursal Actualizada");
        update.setAddress("Carrera 99");
        update.setPhone("3205556666");
        update.setZone("Sur");

        BranchEntity updated = branchService.updateBranch(entityId, update);
        assertEquals("Sucursal Actualizada", updated.getName());
        assertEquals("3205556666", updated.getPhone());
    }

    @Test
    void testUpdateInvalidBranch() {
        BranchEntity update = new BranchEntity();
        update.setName("Sucursal Inexistente");
        update.setAddress("Calle 1");
        update.setPhone("3110000000");
        update.setZone("Este");

        assertThrows(EntityNotFoundException.class, () -> branchService.updateBranch(999L, update));
    }


    @Test
    void testUpdateBranchInvalidData() {
        BranchEntity entity = branchList.get(0);
        Long entityId = entity.getId();
        
        BranchEntity update = new BranchEntity();
        update.setName(" ");
        update.setAddress("Sin dirección");
        update.setPhone("abc123");
        update.setZone("Norte");

        assertThrows(IllegalOperationException.class, () -> branchService.updateBranch(entityId, update));
    }


    @Test
    void testDeleteBranch() throws EntityNotFoundException {
        BranchEntity entity = branchList.get(0);
        Long entityId = entity.getId();
        
        branchService.deleteBranch(entityId);
        BranchEntity deleted = entityManager.find(BranchEntity.class, entityId);
        assertNull(deleted);
    }

    @Test
    void testDeleteInvalidBranch() {
        assertThrows(EntityNotFoundException.class, () -> branchService.deleteBranch(999L));
    }
}