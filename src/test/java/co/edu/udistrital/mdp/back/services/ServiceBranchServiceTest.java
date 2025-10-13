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
@Import({ ServiceBranchService.class, BranchServiceService.class, BranchService.class, ServiceService.class })
class ServiceBranchServiceTest {

    @Autowired
    private ServiceBranchService serviceBranchService;

    @Autowired
    private BranchServiceService branchServiceService;

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
        // Asociamos la primera sucursal al primer servicio
        serviceBranchService.addBranchToService(servicesList.get(0).getId(), branchesList.get(0).getId());
    }

    @Test
    void testAddBranchToService() throws EntityNotFoundException {
        ServiceEntity service = servicesList.get(1);
        BranchEntity branch = branchesList.get(1);

        serviceBranchService.addBranchToService(service.getId(), branch.getId());
        List<BranchEntity> branches = serviceBranchService.getBranches(service.getId());

        assertNotNull(branches);
        assertTrue(branches.contains(branch));
    }

    @Test
    void testAddBranchInvalidService() {
        BranchEntity branch = branchesList.get(0);
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.addBranchToService(0L, branch.getId());
        });
    }

    @Test
    void testAddBranchInvalidBranch() {
        ServiceEntity service = servicesList.get(0);
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.addBranchToService(service.getId(), 0L);
        });
    }

    @Test
    void testGetBranches() throws EntityNotFoundException {
        List<BranchEntity> list = serviceBranchService.getBranches(servicesList.get(0).getId());
        assertEquals(1, list.size());
        assertEquals(branchesList.get(0).getId(), list.get(0).getId());
    }

    @Test
    void testGetBranchesInvalidService() {
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.getBranches(0L);
        });
    }

    @Test
    void testRemoveBranchFromService() throws EntityNotFoundException {
        ServiceEntity service = servicesList.get(0);
        BranchEntity branch = branchesList.get(0);

        serviceBranchService.removeBranchFromService(service.getId(), branch.getId());
        List<BranchEntity> branches = serviceBranchService.getBranches(service.getId());

        assertFalse(branches.contains(branch));
    }

    @Test
    void testRemoveBranchInvalidService() {
        BranchEntity branch = branchesList.get(0);
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.removeBranchFromService(0L, branch.getId());
        });
    }

    @Test
    void testRemoveBranchInvalidBranch() {
        ServiceEntity service = servicesList.get(0);
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.removeBranchFromService(service.getId(), 0L);
        });
    }
}
