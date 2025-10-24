package co.edu.udistrital.mdp.back.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
                entity.setService(null);
                entity.setProduct(null);
            } else if (i == 1) {
                entity.setService(service);
                entity.setBranch(null);
                entity.setProduct(null);
            } else {
                entity.setProduct(product);
                entity.setBranch(null);
                entity.setService(null);
            }

            entityManager.persist(entity);
            multimediaList.add(entity);
        }
    }

    @Test
    @DisplayName("createMultimedia: OK con Branch")
    void testCreateMultimediaWithBranch() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("video/mp4");
        newEntity.setUrl("https://example.com/video.mp4");
        newEntity.setBranch(branch);
        newEntity.setService(null);
        newEntity.setProduct(null);

        MultimediaEntity result = multimediaService.createMultimedia(newEntity);
        assertNotNull(result);

        Long resultId = result.getId();
        MultimediaEntity entity = entityManager.find(MultimediaEntity.class, resultId);
        assertEquals(newEntity.getType(), entity.getType());
        assertEquals(newEntity.getUrl(), entity.getUrl());
        Long branchId = branch.getId();
        assertEquals(branchId, entity.getBranch().getId());
    }

    @Test
    @DisplayName("createMultimedia: OK con Service")
    void testCreateMultimediaWithService() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("image/png");
        newEntity.setUrl("https://example.com/service-image.png");
        newEntity.setBranch(null);
        newEntity.setService(service);
        newEntity.setProduct(null);

        MultimediaEntity result = multimediaService.createMultimedia(newEntity);
        assertNotNull(result);
        assertNotNull(result.getService());
        Long serviceId = service.getId();
        assertEquals(serviceId, result.getService().getId());
    }

    @Test
    @DisplayName("createMultimedia: OK con Product")
    void testCreateMultimediaWithProduct() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("video/webm");
        newEntity.setUrl("https://example.com/product-video.webm");
        newEntity.setBranch(null);
        newEntity.setService(null);
        newEntity.setProduct(product);

        MultimediaEntity result = multimediaService.createMultimedia(newEntity);
        assertNotNull(result);
        assertNotNull(result.getProduct());
        Long productId = product.getId();
        assertEquals(productId, result.getProduct().getId());
    }

    @Test
    @DisplayName("createMultimedia: sin entidad asociada -> IllegalOperationException")
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
    @DisplayName("createMultimedia: múltiples entidades -> IllegalOperationException")
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
    @DisplayName("createMultimedia: tipo inválido -> IllegalOperationException")
    void testCreateMultimediaWithInvalidType() {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("audio/mp3");
        newEntity.setUrl("https://example.com/test.mp3");
        newEntity.setBranch(branch);
        newEntity.setService(null);
        newEntity.setProduct(null);

        assertThrows(IllegalOperationException.class,
                () -> multimediaService.createMultimedia(newEntity));
    }

    @Test
    @DisplayName("createMultimedia: Branch inválido -> EntityNotFoundException")
    void testCreateMultimediaWithInvalidBranch() {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("image/png");
        newEntity.setUrl("https://example.com/test.png");
        BranchEntity invalidBranch = new BranchEntity();
        invalidBranch.setId(999L);
        newEntity.setBranch(invalidBranch);
        newEntity.setService(null);
        newEntity.setProduct(null);

        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.createMultimedia(newEntity));
    }

    @Test
    @DisplayName("createMultimedia: Service inválido -> EntityNotFoundException")
    void testCreateMultimediaWithInvalidService() {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("image/png");
        newEntity.setUrl("https://example.com/test.png");
        ServiceEntity invalidService = new ServiceEntity();
        invalidService.setId(999L);
        newEntity.setBranch(null);
        newEntity.setService(invalidService);
        newEntity.setProduct(null);

        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.createMultimedia(newEntity));
    }

    @Test
    @DisplayName("createMultimedia: Product inválido -> EntityNotFoundException")
    void testCreateMultimediaWithInvalidProduct() {
        MultimediaEntity newEntity = factory.manufacturePojo(MultimediaEntity.class);
        newEntity.setType("image/png");
        newEntity.setUrl("https://example.com/test.png");
        ProductEntity invalidProduct = new ProductEntity();
        invalidProduct.setId(999L);
        newEntity.setBranch(null);
        newEntity.setService(null);
        newEntity.setProduct(invalidProduct);

        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.createMultimedia(newEntity));
    }

    @Test
    @DisplayName("getMultimedias: retorna lista completa")
    void testGetMultimedias() {
        List<MultimediaEntity> list = multimediaService.getMultimedias();
        assertEquals(multimediaList.size(), list.size());
    }

    @Test
    @DisplayName("getMultimedia: OK por ID")
    void testGetMultimedia() throws EntityNotFoundException {
        MultimediaEntity entity = multimediaList.get(0);
        Long entityId = entity.getId();
        
        MultimediaEntity result = multimediaService.getMultimedia(entityId);
        assertNotNull(result);
        assertEquals(entityId, result.getId());
        assertEquals(entity.getType(), result.getType());
        assertEquals(entity.getUrl(), result.getUrl());
    }

    @Test
    @DisplayName("getMultimedia: ID inválido -> EntityNotFoundException")
    void testGetInvalidMultimedia() {
        Long invalidId = 0L;
        
        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.getMultimedia(invalidId));
    }

    @Test
    @DisplayName("updateMultimedia: actualiza type y url")
    void testUpdateMultimedia() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity entity = multimediaList.get(0);
        Long entityId = entity.getId();
        
        MultimediaEntity updateEntity = new MultimediaEntity();
        updateEntity.setType("image/png");
        updateEntity.setUrl("https://example.com/updated.png");

        MultimediaEntity result = multimediaService.updateMultimedia(entityId, updateEntity);

        assertEquals("image/png", result.getType());
        assertEquals("https://example.com/updated.png", result.getUrl());
    }

    @Test
    @DisplayName("updateMultimedia: actualiza solo type")
    void testUpdateMultimediaTypeOnly() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity entity = multimediaList.get(0);
        Long entityId = entity.getId();
        
        MultimediaEntity updateEntity = new MultimediaEntity();
        updateEntity.setType("video/mp4");
        updateEntity.setUrl(null);

        MultimediaEntity result = multimediaService.updateMultimedia(entityId, updateEntity);

        assertEquals("video/mp4", result.getType());
    }

    @Test
    @DisplayName("updateMultimedia: actualiza solo url")
    void testUpdateMultimediaUrlOnly() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity entity = multimediaList.get(0);
        Long entityId = entity.getId();
        
        MultimediaEntity updateEntity = new MultimediaEntity();
        updateEntity.setType(null);
        updateEntity.setUrl("https://example.com/new-url.jpg");

        MultimediaEntity result = multimediaService.updateMultimedia(entityId, updateEntity);

        assertEquals("https://example.com/new-url.jpg", result.getUrl());
    }

    @Test
    @DisplayName("updateMultimedia: ID inválido -> EntityNotFoundException")
    void testUpdateInvalidMultimedia() {
        Long invalidId = 0L;
        MultimediaEntity updateEntity = new MultimediaEntity();
        updateEntity.setType("image/png");

        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.updateMultimedia(invalidId, updateEntity));
    }

    @Test
    @DisplayName("deleteMultimedia: OK cuando hay múltiples multimedia")
    void testDeleteMultimedia() throws EntityNotFoundException, IllegalOperationException {
        MultimediaEntity extra = factory.manufacturePojo(MultimediaEntity.class);
        extra.setType("image/jpeg");
        extra.setUrl("https://example.com/extra.jpg");
        extra.setBranch(branch);
        extra.setService(null);
        extra.setProduct(null);
        entityManager.persist(extra);

        MultimediaEntity entity = multimediaList.get(0);
        Long entityId = entity.getId();
        
        multimediaService.deleteMultimedia(entityId);

        MultimediaEntity deleted = entityManager.find(MultimediaEntity.class, entityId);
        assertNull(deleted);
    }

    @Test
    @DisplayName("deleteMultimedia: ID inválido -> EntityNotFoundException")
    void testDeleteInvalidMultimedia() {
        Long invalidId = 0L;
        
        assertThrows(EntityNotFoundException.class,
                () -> multimediaService.deleteMultimedia(invalidId));
    }

    // Test parametrizado que reemplaza los 3 tests individuales
    @ParameterizedTest(name = "deleteMultimedia: única multimedia de {0} -> IllegalOperationException")
    @MethodSource("provideMultimediaIndicesForDeletion")
    @DisplayName("deleteMultimedia: única multimedia de entidad -> IllegalOperationException")
    void testDeleteOnlyMultimediaOfEntity(String entityType, int multimediaIndex) {
        MultimediaEntity entity = multimediaList.get(multimediaIndex);
        Long entityId = entity.getId();

        assertThrows(IllegalOperationException.class,
                () -> multimediaService.deleteMultimedia(entityId));
    }

    private static Stream<Arguments> provideMultimediaIndicesForDeletion() {
        return Stream.of(
            Arguments.of("Branch", 0),
            Arguments.of("Service", 1),
            Arguments.of("Product", 2)
        );
    }
}