package co.edu.udistrital.mdp.back.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.BranchEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.BranchRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Transactional
    public BranchEntity createBranch(BranchEntity branch) throws IllegalOperationException {
        log.info("Starting branch creation process with name = {}", branch.getName());

        validateBranch(branch);

        if (branch.getId() != null && branchRepository.existsById(branch.getId())) {
            throw new IllegalOperationException("A branch with this ID already exists.");
        }

        BranchEntity savedBranch = branchRepository.save(branch);
        log.info("Branch creation process finished with id = {}", savedBranch.getId());
        return savedBranch;
    }

    public List<BranchEntity> getBranches() {
        log.info("Starting process to retrieve all branches");
        List<BranchEntity> branches = branchRepository.findAll();
        log.info("Finished retrieval process: {} branches found", branches.size());
        return branches;
    }

    public BranchEntity getBranch(Long id) throws EntityNotFoundException {
        log.info("Starting process to find branch with id = {}", id);
        Optional<BranchEntity> branch = branchRepository.findById(id);

        if (branch.isEmpty()) {
            throw new EntityNotFoundException("The branch with the specified ID does not exist.");
        }

        log.info("Finished process to find branch with id = {}", id);
        return branch.get();
    }

    @Transactional
    public BranchEntity updateBranch(Long id, BranchEntity branch)
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting branch update process with id = {}", id);

        BranchEntity existing = getBranch(id);
        validateBranch(branch);

        existing.setName(branch.getName());
        existing.setAddress(branch.getAddress());
        existing.setPhone(branch.getPhone());
        existing.setZone(branch.getZone());

        BranchEntity updatedBranch = branchRepository.save(existing);
        log.info("Branch update process finished with id = {}", id);
        return updatedBranch;
    }

    @Transactional
    public void deleteBranch(Long id) throws EntityNotFoundException {
        log.info("Starting branch deletion process with id = {}", id);
        BranchEntity branch = getBranch(id);
        branchRepository.delete(branch);
        log.info("Branch deletion process finished with id = {}", id);
    }

    private void validateBranch(BranchEntity branch) throws IllegalOperationException {
        if (branch.getName() == null || branch.getName().trim().isEmpty()) {
            throw new IllegalOperationException("The branch name is required.");
        }

        if (branch.getPhone() == null || !branch.getPhone().matches("\\d{7,10}")) {
            throw new IllegalOperationException("The phone number must contain between 7 and 10 digits.");
        }
    }
}
