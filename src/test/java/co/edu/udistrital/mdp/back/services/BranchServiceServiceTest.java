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
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import({ BranchServiceService.class, BranchService.class, ServiceService.class, ServiceBranchService.class })
class BranchServiceServiceTest {

    @Autowired
    private BranchServiceService branchServiceService;

    @Autowired
    private ServiceBranchService serviceBranchService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private List<BranchEntity> branchesList = new ArrayList<>();
    private List<ServiceEntity> servicesList = new ArrayList<>();

    @BeforeEach
    void setUp() throws IllegalOperationException, EntityNotFoundException {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from BranchEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from ServiceEntity").executeUpdate();
    }

    private void insertData() throws IllegalOperationException, EntityNotFoundException {
        for (int i = 0; i < 3; i++) {
            BranchEntity branch = factory.manufacturePojo(BranchEntity.class);
            branch = branchService.createBranch(branch);
            branchesList.add(branch);
        }
        for (int i = 0; i < 3; i++) {
            ServiceEntity service = factory.manufacturePojo(ServiceEntity.class);
            service = serviceService.save(service);
            servicesList.add(service);
        }
      
        branchServiceService.addServiceToBranch(branchesList.get(0).getId(), servicesList.get(0).getId());
    }

    @Test
    void testAddServiceToBranch() throws EntityNotFoundException {
        BranchEntity branch = branchesList.get(1);
        ServiceEntity service = servicesList.get(1);

        branchServiceService.addServiceToBranch(branch.getId(), service.getId());
        List<ServiceEntity> services = branchServiceService.getServices(branch.getId());

        assertNotNull(services);
        assertTrue(services.contains(service));
    }

    @Test
    void testAddServiceInvalidBranch() {
        ServiceEntity service = servicesList.get(0);
        assertThrows(EntityNotFoundException.class, () -> {
            branchServiceService.addServiceToBranch(0L, service.getId());
        });
    }

    @Test
    void testAddServiceInvalidService() {
        BranchEntity branch = branchesList.get(0);
        assertThrows(EntityNotFoundException.class, () -> {
            branchServiceService.addServiceToBranch(branch.getId(), 0L);
        });
    }

    @Test
    void testGetServices() throws EntityNotFoundException {
        List<ServiceEntity> list = branchServiceService.getServices(branchesList.get(0).getId());
        assertEquals(1, list.size());
        assertEquals(servicesList.get(0).getId(), list.get(0).getId());
    }

    @Test
    void testGetServicesInvalidBranch() {
        assertThrows(EntityNotFoundException.class, () -> {
            branchServiceService.getServices(0L);
        });
    }

    @Test
    void testRemoveServiceFromBranch() throws EntityNotFoundException {
        BranchEntity branch = branchesList.get(0);
        ServiceEntity service = servicesList.get(0);

        branchServiceService.removeServiceFromBranch(branch.getId(), service.getId());
        List<ServiceEntity> services = branchServiceService.getServices(branch.getId());

        assertFalse(services.contains(service));
    }

    @Test
    void testRemoveServiceInvalidBranch() {
        ServiceEntity service = servicesList.get(0);
        assertThrows(EntityNotFoundException.class, () -> {
            branchServiceService.removeServiceFromBranch(0L, service.getId());
        });
    }

    @Test
    void testRemoveServiceInvalidService() {
        BranchEntity branch = branchesList.get(0);
        assertThrows(EntityNotFoundException.class, () -> {
            branchServiceService.removeServiceFromBranch(branch.getId(), 0L);
        });
    }
}
