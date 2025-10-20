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
import co.edu.udistrital.mdp.back.entities.MultimediaEntity;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import(MultimediaService.class)
class MultimediaServiceTest {

    @Autowired
    private MultimediaService multimediaService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private List<MultimediaEntity> multimediaList = new ArrayList<>();
    private BranchEntity branch;
    private ServiceEntity service;
    private ProductEntity product;

    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from MultimediaEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from BranchEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from ServiceEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from ProductEntity").executeUpdate();
    }

    private void insertData() {
        branch = factory.manufacturePojo(BranchEntity.class);
        entityManager.persist(branch);

        service = factory.manufacturePojo(ServiceEntity.class);
        entityManager.persist(service);

        product = factory.manufacturePojo(ProductEntity.class);
        entityManager.persist(product);

        for (int i = 0; i < 3; i++) {
            MultimediaEntity entity = factory.manufacturePojo(MultimediaEntity.class);
            entity.setType("image/jpeg");
            entity.setUrl("https://example.com/image" + i + ".jpg");

            if (i == 0) {
                entity.setBranch(branch); 
            }else if (i == 1) {
                entity.setService(service); 
            }else {
                entity.setProduct(product);
            }

            entityManager.persist(entity);
            multimediaList.add(entity);
        }
    }

    @Test
    void testCreateMultimediaWithBranch() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("video/mp4");
        newEntity.setUrl("https://example.com/video.mp4");
        newEntity.setBranch(branch);
        newEntity.setService(null);
        newEntity.setProduct(null);

        MultimediaEntity result = multimediaService.createMultimedia(newEntity);
        assertNotNull(result);

        MultimediaEntity entity = entityManager.find(MultimediaEntity.class, result.getId());
        assertEquals(newEntity.getType(), entity.getType());
        assertEquals(newEntity.getUrl(), entity.getUrl());
        assertEquals(branch.getId(), entity.getBranch().getId());
    }

    @Test
    void testCreateMultimediaWithoutEntity() {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("image/png");
        newEntity.setUrl("https://example.com/test.png");
        newEntity.setBranch(null);
        newEntity.setService(null);
        newEntity.setProduct(null);

        assertThrows(IllegalOperationException.class,
                () -> multimediaService.createMultimedia(newEntity));
    }

    @Test
    void testCreateMultimediaWithMultipleEntities() {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("image/png");
        newEntity.setUrl("https://example.com/test.png");
        newEntity.setBranch(branch);
        newEntity.setService(service);
        newEntity.setProduct(null);

        assertThrows(IllegalOperationException.class,
                () -> multimediaService.createMultimedia(newEntity));
    }

    @Test
    void testCreateMultimediaWithInvalidType() {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("audio/mp3");
        newEntity.setUrl("https://example.com/test.mp3");
        newEntity.setBranch(branch);

        assertThrows(IllegalOperationException.class,
                () -> multimediaService.createMultimedia(newEntity));
    }

    @Test
    void testCreateMultimediaWithInvalidBranch() {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("image/png");
        newEntity.setUrl("https://example.com/test.png");
        BranchEntity invalidBranch = new BranchEntity();
        invalidBranch.setId(0L);
        newEntity.setBranch(invalidBranch);

        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.createMultimedia(newEntity));
    }

    @Test
    void testGetMultimedias() {
        List<MultimediaEntity> list = multimediaService.getMultimedias();
        assertEquals(multimediaList.size(), list.size());
    }

    @Test
    void testGetMultimedia() throws EntityNotFoundException {
        MultimediaEntity entity = multimediaList.get(0);
        MultimediaEntity result = multimediaService.getMultimedia(entity.getId());
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getType(), result.getType());
        assertEquals(entity.getUrl(), result.getUrl());
    }

    @Test
    void testGetInvalidMultimedia() {
        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.getMultimedia(0L));
    }

    @Test
    void testUpdateMultimedia() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity entity = multimediaList.get(0);
        MultimediaEntity updateEntity = new MultimediaEntity();
        updateEntity.setType("image/png");
        updateEntity.setUrl("https://example.com/updated.png");

        MultimediaEntity result = multimediaService.updateMultimedia(entity.getId(), updateEntity);

        assertEquals("image/png", result.getType());
        assertEquals("https://example.com/updated.png", result.getUrl());
    }

    @Test
    void testUpdateInvalidMultimedia() {
        MultimediaEntity updateEntity = new MultimediaEntity();
        updateEntity.setType("image/png");

        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.updateMultimedia(0L, updateEntity));
    }

    @Test
    void testDeleteMultimedia() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity extra = factory.manufacturePojo(MultimediaEntity.class);
        extra.setType("image/jpeg");
        extra.setUrl("https://example.com/extra.jpg");
        extra.setBranch(branch);
        entityManager.persist(extra);

        MultimediaEntity entity = multimediaList.get(0);
        multimediaService.deleteMultimedia(entity.getId());

        MultimediaEntity deleted = entityManager.find(MultimediaEntity.class, entity.getId());
        assertNull(deleted);
    }

    @Test
    void testDeleteInvalidMultimedia() {
        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.deleteMultimedia(0L));
    }

    @Test
    void testDeleteOnlyMultimedia() {
        MultimediaEntity entity = multimediaList.get(0);
        Long id = entity.getId();

        assertThrows(IllegalOperationException.class,
                () -> multimediaService.deleteMultimedia(id));
    }
}
