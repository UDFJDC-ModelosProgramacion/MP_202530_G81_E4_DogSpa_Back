package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.ReservationRepository;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // Validar que el precio sea mayor o igual a 0
    private void validatePrice(Double price) throws IllegalOperationException {
        if (price == null || price < 0) {
            throw new IllegalOperationException("Price must be greater than or equal to 0");
        }
    }

    // Crear o actualizar un servicio
    @Transactional
    public ServiceEntity save(ServiceEntity service) throws IllegalOperationException {
        validatePrice(service.getPrice());
        return serviceRepository.save(service);
    }

    // Eliminar un servicio solo si no tiene reservas activas
    @Transactional
    public void delete(Long serviceId) throws EntityNotFoundException, IllegalOperationException {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        int activeReservations = reservationRepository.countByServiceIdAndStatus(serviceId, "SCHEDULED");

        if (activeReservations > 0) {
            throw new IllegalOperationException("Cannot delete service with active reservations");
        }

        serviceRepository.delete(service);
    }

}