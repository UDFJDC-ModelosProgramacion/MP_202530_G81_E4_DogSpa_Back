package co.edu.udistrital.mdp.back.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.BranchEntity;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.repositories.ServiceRepository;
import co.edu.udistrital.mdp.back.repositories.BranchRepository;


import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BranchServiceService {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Transactional
    public BranchEntity addServiceToBranch(Long branchId, Long serviceId) throws EntityNotFoundException {
        log.info("Starting process to add service id={} to branch id={}", serviceId, branchId);

        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        if (!branch.getServices().contains(service)) {
            branch.getServices().add(service);
        }

        log.info("Finished process to add service id={} to branch id={}", serviceId, branchId);
        return branchRepository.save(branch);
    }

    @Transactional
    public BranchEntity removeServiceFromBranch(Long branchId, Long serviceId) throws EntityNotFoundException {
        log.info("Starting process to remove service id={} from branch id={}", serviceId, branchId);

        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        branch.getServices().remove(service);

        log.info("Finished process to remove service id={} from branch id={}", serviceId, branchId);
        return branchRepository.save(branch);
    }
        @Transactional(readOnly = true)
        public List<ServiceEntity> getServices(Long branchId) throws EntityNotFoundException {
            log.info("Fetching services for branch with id = {}", branchId);
            BranchEntity branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
            return branch.getServices();
        }

    @Transactional(readOnly = true)
    public List<ServiceEntity> getServicesOfBranch(Long branchId) throws EntityNotFoundException {
        log.info("Retrieving services for branch id={}", branchId);

        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        return branch.getServices();
    }
}
