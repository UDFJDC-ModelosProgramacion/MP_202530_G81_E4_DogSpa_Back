package co.edu.udistrital.mdp.back.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.BranchEntity;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.repositories.BranchRepository;
import co.edu.udistrital.mdp.back.repositories.ServiceRepository;

@Service
public class ServiceBranchService {

    private static final String SERVICE_NOT_FOUND = "Service not found";
    private static final String BRANCH_NOT_FOUND = "Branch not found";

    private final ServiceRepository serviceRepository;
    private final BranchRepository branchRepository;

    // Constructor injection
    public ServiceBranchService(ServiceRepository serviceRepository, BranchRepository branchRepository) {
        this.serviceRepository = serviceRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional
    public void addBranchToService(Long serviceId, Long branchId) throws EntityNotFoundException {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException(SERVICE_NOT_FOUND));

        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException(BRANCH_NOT_FOUND));

        if (!service.getBranches().contains(branch)) {
            service.getBranches().add(branch);
            branch.getServices().add(service);
        }

        serviceRepository.save(service);
        branchRepository.save(branch);
    }

    @Transactional
    public void removeBranchFromService(Long serviceId, Long branchId) throws EntityNotFoundException {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException(SERVICE_NOT_FOUND));

        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException(BRANCH_NOT_FOUND));

        service.getBranches().remove(branch);
        branch.getServices().remove(service);

        serviceRepository.save(service);
        branchRepository.save(branch);
    }

    @Transactional(readOnly = true)
    public List<BranchEntity> getBranches(Long serviceId) throws EntityNotFoundException {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException(SERVICE_NOT_FOUND));
        return service.getBranches();
    }
}