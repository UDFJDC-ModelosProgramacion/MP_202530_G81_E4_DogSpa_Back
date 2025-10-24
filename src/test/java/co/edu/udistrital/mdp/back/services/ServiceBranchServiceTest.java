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
    void setUp() {
        clearData();
        try {
            insertData();
        } catch (IllegalOperationException | EntityNotFoundException e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("delete from BranchEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from ServiceEntity").executeUpdate();
    }

    private BranchEntity createValidBranch(String nameSuffix) {
        BranchEntity branch = new BranchEntity();
        branch.setName("Sucursal " + nameSuffix);
        branch.setAddress("Calle 123 #" + nameSuffix);
        branch.setPhone("31245678" + nameSuffix); 
        branch.setZone("Zona " + nameSuffix);
        return branch;
    }


    private ServiceEntity createValidService(String nameSuffix) {
        ServiceEntity service = new ServiceEntity();
        service.setName("Servicio " + nameSuffix);
        service.setDescription("Descripción del servicio " + nameSuffix);
        service.setPrice(50000.0);
        return service;
    }

    private void insertData() throws IllegalOperationException, EntityNotFoundException {
        for (int i = 0; i < 3; i++) {
            BranchEntity branch = createValidBranch(String.valueOf(i));
            branch = branchService.createBranch(branch);
            branchesList.add(branch);
        }
        for (int i = 0; i < 3; i++) {
            ServiceEntity service = createValidService(String.valueOf(i));
            service = serviceService.save(service);
            servicesList.add(service);
        }

        serviceBranchService.addBranchToService(servicesList.get(0).getId(), branchesList.get(0).getId());
    }

    @Test
    void testAddBranchToService() throws EntityNotFoundException {
        ServiceEntity service = servicesList.get(1);
        BranchEntity branch = branchesList.get(1);
        Long serviceId = service.getId();
        Long branchId = branch.getId();

        serviceBranchService.addBranchToService(serviceId, branchId);
        List<BranchEntity> branches = serviceBranchService.getBranches(serviceId);

        assertNotNull(branches);
        assertTrue(branches.contains(branch));
    }

    @Test
    void testAddBranchInvalidService() {
        BranchEntity branch = branchesList.get(0);
        Long branchId = branch.getId();
        Long invalidServiceId = 0L;
        
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.addBranchToService(invalidServiceId, branchId);
        });
    }

    @Test
    void testAddBranchInvalidBranch() {
        ServiceEntity service = servicesList.get(0);
        Long serviceId = service.getId();
        Long invalidBranchId = 0L;
        
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.addBranchToService(serviceId, invalidBranchId);
        });
    }

    @Test
    void testGetBranches() throws EntityNotFoundException {
        ServiceEntity service = servicesList.get(0);
        Long serviceId = service.getId();
        BranchEntity expectedBranch = branchesList.get(0);
        Long expectedBranchId = expectedBranch.getId();
        
        List<BranchEntity> list = serviceBranchService.getBranches(serviceId);
        assertEquals(1, list.size());
        assertEquals(expectedBranchId, list.get(0).getId());
    }

    @Test
    void testGetBranchesInvalidService() {
        Long invalidServiceId = 0L;
        
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.getBranches(invalidServiceId);
        });
    }

    @Test
    void testRemoveBranchFromService() throws EntityNotFoundException {
        ServiceEntity service = servicesList.get(0);
        BranchEntity branch = branchesList.get(0);
        Long serviceId = service.getId();
        Long branchId = branch.getId();

        serviceBranchService.removeBranchFromService(serviceId, branchId);
        List<BranchEntity> branches = serviceBranchService.getBranches(serviceId);

        assertFalse(branches.contains(branch));
    }

    @Test
    void testRemoveBranchInvalidService() {
        BranchEntity branch = branchesList.get(0);
        Long branchId = branch.getId();
        Long invalidServiceId = 0L;
        
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.removeBranchFromService(invalidServiceId, branchId);
        });
    }

    @Test
    void testRemoveBranchInvalidBranch() {
        ServiceEntity service = servicesList.get(0);
        Long serviceId = service.getId();
        Long invalidBranchId = 0L;
        
        assertThrows(EntityNotFoundException.class, () -> {
            serviceBranchService.removeBranchFromService(serviceId, invalidBranchId);
        });
    }
}