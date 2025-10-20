package co.edu.udistrital.mdp.back.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.BranchEntity;
import co.edu.udistrital.mdp.back.entities.MultimediaEntity;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.BranchRepository;
import co.edu.udistrital.mdp.back.repositories.MultimediaRepository;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import co.edu.udistrital.mdp.back.repositories.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for Multimedia business logic
 * 
 * @author Alexander Morales Ujueta
 */

@Slf4j
@Service
@RequiredArgsConstructor  // Genera el constructor para inyección de dependencias
public class MultimediaService {

    // Campos final para inyección por constructor
    private final MultimediaRepository multimediaRepository;
    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;
    private final ProductRepository productRepository;

    // Constantes para mensajes de error duplicados
    private static final String MULTIMEDIA_NOT_FOUND = "Multimedia with id = %d not found";
    private static final String BRANCH_NOT_FOUND = "Branch with id = %d not found";
    private static final String SERVICE_NOT_FOUND = "Service with id = %d not found";
    private static final String PRODUCT_NOT_FOUND = "Product with id = %d not found";
    private static final String NOT_FOUND_SUFFIX = " not found";

    // Tipos de multimedia soportados
    private static final List<String> SUPPORTED_TYPES = Arrays.asList("image", "video");

    /**
     * Returns all multimedia elements
     * 
     * @return List of all multimedia elements
     */
    @Transactional
    public List<MultimediaEntity> getMultimedias() {
        log.info("Starting process to query all multimedia elements");
        return multimediaRepository.findAll();
    }

    /**
     * Finds a multimedia element by ID
     * 
     * @param multimediaId The ID of the multimedia to find
     * @return The multimedia found
     * @throws EntityNotFoundException If the multimedia does not exist
     */
    @Transactional
    public MultimediaEntity getMultimedia(Long multimediaId) throws EntityNotFoundException {
        log.info("Starting process to query multimedia with id = {}", multimediaId);
        MultimediaEntity multimediaEntity = multimediaRepository.findById(multimediaId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(MULTIMEDIA_NOT_FOUND, multimediaId)));
        log.info("Finishing process to query multimedia with id = {}", multimediaId);
        return multimediaEntity;
    }

    /**
     * Creates a new multimedia element
     * 
     * Business Rule: Cannot associate multimedia without a valid entity
     * 
     * @param multimediaEntity The multimedia to create
     * @return The created multimedia
     * @throws IllegalOperationException If there is no valid associated entity
     * @throws EntityNotFoundException If associated entity does not exist
     */
    @Transactional
    public MultimediaEntity createMultimedia(MultimediaEntity multimediaEntity)
            throws IllegalOperationException, EntityNotFoundException {
        log.info("Starting process to create multimedia");

        if (multimediaEntity.getBranch() == null && 
            multimediaEntity.getService() == null && 
            multimediaEntity.getProduct() == null) {
            throw new IllegalOperationException(
                    "Cannot create multimedia without a valid associated entity (Branch, Service or Product)");
        }

        int associatedEntities = 0;
        if (multimediaEntity.getBranch() != null) associatedEntities++;
        if (multimediaEntity.getService() != null) associatedEntities++;
        if (multimediaEntity.getProduct() != null) associatedEntities++;

        if (associatedEntities > 1) {
            throw new IllegalOperationException(
                    "Multimedia can only be associated with one entity (Branch, Service or Product)");
        }

        if (multimediaEntity.getBranch() != null) {
            BranchEntity branch = branchRepository.findById(multimediaEntity.getBranch().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format(BRANCH_NOT_FOUND, multimediaEntity.getBranch().getId())));
            multimediaEntity.setBranch(branch);
        }

        if (multimediaEntity.getService() != null) {
            ServiceEntity service = serviceRepository.findById(multimediaEntity.getService().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format(SERVICE_NOT_FOUND, multimediaEntity.getService().getId())));
            multimediaEntity.setService(service);
        }

        if (multimediaEntity.getProduct() != null) {
            ProductEntity product = productRepository.findById(multimediaEntity.getProduct().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format(PRODUCT_NOT_FOUND, multimediaEntity.getProduct().getId())));
            multimediaEntity.setProduct(product);
        }

        validateMediaType(multimediaEntity);

        log.info("Finishing process to create multimedia");
        return multimediaRepository.save(multimediaEntity);
    }

    /**
     * Updates a multimedia element
     * 
     * Business Rule: Cannot update multimedia with corrupted or unsupported file
     * 
     * @param multimediaId The multimedia identifier
     * @param multimedia The multimedia with updated data
     * @return The updated multimedia
     * @throws EntityNotFoundException If multimedia does not exist
     * @throws IllegalOperationException If file is corrupted or unsupported
     */
    @Transactional
    public MultimediaEntity updateMultimedia(Long multimediaId, MultimediaEntity multimedia)
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to update multimedia with id = {}", multimediaId);

        MultimediaEntity multimediaEntity = multimediaRepository.findById(multimediaId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(MULTIMEDIA_NOT_FOUND, multimediaId)));

        if (multimedia.getType() != null || multimedia.getUrl() != null) {
            MultimediaEntity tempMultimedia = new MultimediaEntity();
            tempMultimedia.setType(multimedia.getType() != null ? multimedia.getType() : multimediaEntity.getType());
            tempMultimedia.setUrl(multimedia.getUrl() != null ? multimedia.getUrl() : multimediaEntity.getUrl());
            
            validateMediaType(tempMultimedia);
            validateUrlIntegrity(tempMultimedia.getUrl());
        }

        if (multimedia.getType() != null) {
            multimediaEntity.setType(multimedia.getType());
        }
        if (multimedia.getUrl() != null) {
            multimediaEntity.setUrl(multimedia.getUrl());
        }

        log.info("Finishing process to update multimedia with id = {}", multimediaId);
        return multimediaRepository.save(multimediaEntity);
    }

    /**
     * Deletes a multimedia element
     * 
     * Business Rule: Cannot delete multimedia if it is primary in the entity
     * 
     * @param multimediaId The multimedia identifier
     * @throws EntityNotFoundException If multimedia does not exist
     * @throws IllegalOperationException If it is the primary multimedia of the entity
     */
    @Transactional
    public void deleteMultimedia(Long multimediaId) throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to delete multimedia with id = {}", multimediaId);

        MultimediaEntity multimedia = multimediaRepository.findById(multimediaId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(MULTIMEDIA_NOT_FOUND, multimediaId)));

        if (multimedia.getBranch() != null) {
            long count = multimediaRepository.findAll().stream()
                    .filter(m -> m.getBranch() != null && m.getBranch().getId().equals(multimedia.getBranch().getId()))
                    .count();
            if (count == 1) {
                throw new IllegalOperationException("Cannot delete the only multimedia of the branch");
            }
        }

        if (multimedia.getService() != null) {
            long count = multimediaRepository.findAll().stream()
                    .filter(m -> m.getService() != null && m.getService().getId().equals(multimedia.getService().getId()))
                    .count();
            if (count == 1) {
                throw new IllegalOperationException("Cannot delete the only multimedia of the service");
            }
        }

        if (multimedia.getProduct() != null) {
            long count = multimediaRepository.findAll().stream()
                    .filter(m -> m.getProduct() != null && m.getProduct().getId().equals(multimedia.getProduct().getId()))
                    .count();
            if (count == 1) {
                throw new IllegalOperationException("Cannot delete the only multimedia of the product");
            }
        }

        multimediaRepository.delete(multimedia);
        log.info("Finishing process to delete multimedia with id = {}", multimediaId);
    }

    /**
     * Validates multimedia file type
     * 
     * @param multimedia The multimedia element to validate
     * @throws IllegalOperationException If type is not supported
     */
    private void validateMediaType(MultimediaEntity multimedia) throws IllegalOperationException {
        if (multimedia.getType() == null || multimedia.getType().trim().isEmpty()) {
            throw new IllegalOperationException("Multimedia type cannot be empty");
        }

        String type = multimedia.getType().toLowerCase();
        
        if (!SUPPORTED_TYPES.stream().anyMatch(type::contains)) {
            throw new IllegalOperationException(
                    "Unsupported file type: " + multimedia.getType() + 
                    ". Supported types: image, video");
        }
    }

    /**
     * Validates file URL integrity
     * 
     * @param url The URL to validate
     * @throws IllegalOperationException If URL is corrupted or invalid
     */
    private void validateUrlIntegrity(String url) throws IllegalOperationException {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalOperationException("File URL cannot be empty");
        }

        if (!url.matches("^(https?|ftp)://.*$")) {
            throw new IllegalOperationException(
                    "Invalid URL or corrupted file. URL must start with http://, https:// or ftp://");
        }

        String lowerUrl = url.toLowerCase();
        boolean hasValidExtension = false;
        
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        String[] videoExtensions = {".mp4", ".mpeg", ".webm", ".avi", ".mov"};
        
        for (String ext : imageExtensions) {
            if (lowerUrl.endsWith(ext)) {
                hasValidExtension = true;
                break;
            }
        }
        
        if (!hasValidExtension) {
            for (String ext : videoExtensions) {
                if (lowerUrl.endsWith(ext)) {
                    hasValidExtension = true;
                    break;
                }
            }
        }

        if (!hasValidExtension) {
            throw new IllegalOperationException("Unsupported file extension or corrupted file");
        }
    }