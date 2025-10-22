package co.edu.udistrital.mdp.back.services;

import java.util.List;

import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.ReservationRepository;
import co.edu.udistrital.mdp.back.repositories.ServiceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceService {

    private static final String SERVICE_NOT_FOUND_MESSAGE = "Service not found";

    private final ServiceRepository serviceRepository;
    private final ReservationRepository reservationRepository;

    // Constructor injection
    public ServiceService(ServiceRepository serviceRepository, ReservationRepository reservationRepository) {
        this.serviceRepository = serviceRepository;
        this.reservationRepository = reservationRepository;
    }

    // Validar que el precio sea mayor o igual a 0
    private void validatePrice(Double price) throws IllegalOperationException {
        if (price == null || price < 0) {
            throw new IllegalOperationException("Price must be greater than or equal to 0");
        }
    }

    // Obtener todos los servicios
    @Transactional(readOnly = true)
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    // Obtener un servicio por su ID
    @Transactional(readOnly = true)
    public ServiceEntity getServiceById(Long serviceId) throws EntityNotFoundException {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException(SERVICE_NOT_FOUND_MESSAGE));
    }
    
    // Crear un servicio o actualizar uno existente
    @Transactional
    public ServiceEntity save(ServiceEntity service) throws IllegalOperationException {
        validatePrice(service.getPrice());
        return serviceRepository.save(service);
    }

    // Actualizar un servicio
    @Transactional
    public ServiceEntity updateService(Long id, ServiceEntity newData) throws EntityNotFoundException {
        ServiceEntity existing = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service not found with id " + id));

        // Solo actualizas los campos que deben cambiar
        existing.setName(newData.getName());
        existing.setDescription(newData.getDescription());
        existing.setPrice(newData.getPrice());
        existing.setDuration(newData.getDuration());

        return serviceRepository.save(existing);
    }

    // Eliminar un servicio solo si no tiene reservas activas
    @Transactional
    public void delete(Long serviceId) throws EntityNotFoundException, IllegalOperationException {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException(SERVICE_NOT_FOUND_MESSAGE));

        int activeReservations = reservationRepository.countByService_IdAndReservationStatus(serviceId, "SCHEDULED");

        if (activeReservations > 0) {
            throw new IllegalOperationException("Cannot delete service with active reservations");
        }

        serviceRepository.delete(service);
    }
}