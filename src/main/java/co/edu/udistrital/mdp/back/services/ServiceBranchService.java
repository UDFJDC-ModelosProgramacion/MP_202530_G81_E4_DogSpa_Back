package co.edu.udistrital.mdp.back.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.BranchEntity;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.repositories.BranchRepository;
import co.edu.udistrital.mdp.back.repositories.ServiceRepository;

@Service
public class ServiceBranchService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Transactional
    public void addBranchToService(Long serviceId, Long branchId) throws EntityNotFoundException {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

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
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        BranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        service.getBranches().remove(branch);
        branch.getServices().remove(service);

        serviceRepository.save(service);
        branchRepository.save(branch);
    }

    @Transactional
    public List<BranchEntity> getBranches(Long serviceId) throws EntityNotFoundException {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));
        return service.getBranches();
    }
}
