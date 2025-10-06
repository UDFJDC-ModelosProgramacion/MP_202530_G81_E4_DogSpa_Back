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
public class BranchService{

    @Autowired
    private BranchRepository branchRepository;

 
    @Transactional
    public BranchEntity createBranch(BranchEntity branch) throws IllegalOperationException {
        log.info("Inicia proceso de creación de sucursal con nombre = {}", branch.getName());

        validateBranch(branch);

        if (branch.getId() != null && branchRepository.existsById(branch.getId())) {
            throw new IllegalOperationException("Ya existe una sucursal con este ID.");
        }

        BranchEntity savedBranch = branchRepository.save(branch);
        log.info("Termina proceso de creación de sucursal con id = {}", savedBranch.getId());
        return savedBranch;
    }

    public List<BranchEntity> getBranches() {
        log.info("Inicia proceso de consultar todas las sucursales");
        List<BranchEntity> branches = branchRepository.findAll();
        log.info("Termina proceso de consulta: se encontraron {} sucursales", branches.size());
        return branches;
    }


    public BranchEntity getBranch(Long id) throws EntityNotFoundException {
        log.info("Inicia proceso de búsqueda de sucursal con id = {}", id);
        Optional<BranchEntity> branch = branchRepository.findById(id);

        if (branch.isEmpty()) {
            throw new EntityNotFoundException("La sucursal con el id especificado no existe.");
        }

        log.info("Termina proceso de búsqueda de sucursal con id = {}", id);
        return branch.get();
    }

   
    @Transactional
    public BranchEntity updateBranch(Long id, BranchEntity branch)
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Inicia proceso de actualización de sucursal con id = {}", id);

        BranchEntity existing = getBranch(id);
        validateBranch(branch);

        existing.setName(branch.getName());
        existing.setAddress(branch.getAddress());
        existing.setPhone(branch.getPhone());
        existing.setZone(branch.getZone());

        BranchEntity updatedBranch = branchRepository.save(existing);
        log.info("Termina proceso de actualización de sucursal con id = {}", id);
        return updatedBranch;
    }

    @Transactional
    public void deleteBranch(Long id) throws EntityNotFoundException {
        log.info("Inicia proceso de eliminación de sucursal con id = {}", id);
        BranchEntity branch = getBranch(id);
        branchRepository.delete(branch);
        log.info("Termina proceso de eliminación de sucursal con id = {}", id);
    }

   
    private void validateBranch(BranchEntity branch) throws IllegalOperationException {
        if (branch.getName() == null || branch.getName().trim().isEmpty()) {
            throw new IllegalOperationException("El nombre de la sucursal es obligatorio.");
        }

        if (branch.getPhone() == null || !branch.getPhone().matches("\\d{7,10}")) {
            throw new IllegalOperationException("El teléfono debe tener entre 7 y 10 dígitos numéricos.");
        }
    }
}
