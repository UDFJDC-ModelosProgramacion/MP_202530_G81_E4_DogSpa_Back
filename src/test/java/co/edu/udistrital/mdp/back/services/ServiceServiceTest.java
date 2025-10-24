package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.ReservationRepository;
import co.edu.udistrital.mdp.back.repositories.ServiceRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ServiceService.class)
class ServiceServiceTest {

    @Autowired
    private ServiceService serviceService;

    @MockBean private ServiceRepository serviceRepository;
    @MockBean private ReservationRepository reservationRepository;
    @MockBean private OrderDetailRepository orderDetailRepository; // inyectado en el service (aunque no se use aquí)

    private ServiceEntity grooming;

    @BeforeEach
    void setUp() {
        grooming = new ServiceEntity();
        grooming.setId(1L);
        grooming.setName("Grooming");
        grooming.setPrice(30.0);
        // agrega otros campos si tu entidad los requiere obligatoriamente
    }

    // -------- save --------

    @Test
    @DisplayName("save: precio válido (incluye 0) -> guarda en el repositorio")
    void save_validPrice_ok() throws Exception {
        when(serviceRepository.save(any(ServiceEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var saved = serviceService.save(grooming);

        assertNotNull(saved);
        assertEquals(30.0, saved.getPrice());
        verify(serviceRepository).save(grooming);
        verifyNoInteractions(reservationRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("save: price = 0 es permitido")
    void save_zeroPrice_ok() throws Exception {
        grooming.setPrice(0.0);
        when(serviceRepository.save(any(ServiceEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var saved = serviceService.save(grooming);

        assertEquals(0.0, saved.getPrice());
        verify(serviceRepository).save(grooming);
        verifyNoInteractions(reservationRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("save: price null o negativo -> IllegalOperationException")
    void save_invalidPrice_throws() {
        grooming.setPrice(null);
        assertThrows(IllegalOperationException.class, () -> serviceService.save(grooming));

        grooming.setPrice(-5.0);
        assertThrows(IllegalOperationException.class, () -> serviceService.save(grooming));

        verifyNoInteractions(serviceRepository, reservationRepository, orderDetailRepository);
    }

    // -------- delete --------

    @Test
    @DisplayName("delete: servicio no existe -> EntityNotFoundException")
    void delete_notFound_throws() {
        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> serviceService.delete(99L));

        verify(serviceRepository).findById(99L);
        verifyNoMoreInteractions(serviceRepository);
        verifyNoInteractions(reservationRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("delete: con reservas activas (SCHEDULED) -> IllegalOperationException")
    void delete_withActiveReservations_throws() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(grooming));
        when(reservationRepository.countByService_IdAndReservationStatus(1L, "SCHEDULED")).thenReturn(3);

        assertThrows(IllegalOperationException.class, () -> serviceService.delete(1L));

        verify(serviceRepository).findById(1L);
        verify(reservationRepository).countByService_IdAndReservationStatus(1L, "SCHEDULED");
        verifyNoMoreInteractions(serviceRepository, reservationRepository);
        verifyNoInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("delete: sin reservas activas -> elimina el servicio")
    void delete_noActiveReservations_ok() throws Exception {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(grooming));
        when(reservationRepository.countByService_IdAndReservationStatus(1L, "SCHEDULED")).thenReturn(0);

        serviceService.delete(1L);

        verify(serviceRepository).findById(1L);
        verify(reservationRepository).countByService_IdAndReservationStatus(1L, "SCHEDULED");
        verify(serviceRepository).delete(grooming);
        verifyNoMoreInteractions(serviceRepository, reservationRepository);
        verifyNoInteractions(orderDetailRepository);
    }
    @Test
    @DisplayName("save lanza IllegalOperationException si el precio es negativo")
    void save_invalidPrice_throwsException() {
        ServiceEntity s = new ServiceEntity();
        s.setPrice(-10.0);
        assertThrows(IllegalOperationException.class, () -> serviceService.save(s));
    }

    @Test
    @DisplayName("getServiceById lanza EntityNotFoundException si no existe")
    void getServiceById_notFound_throwsException() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> serviceService.getServiceById(1L));
    }

    @Test
    @DisplayName("delete lanza IllegalOperationException si hay reservas activas")
    void delete_withActiveReservations_throwsException() {
        ServiceEntity s = new ServiceEntity();
        s.setId(1L);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(s));
        when(reservationRepository.countByService_IdAndReservationStatus(1L, "SCHEDULED"))
            .thenReturn(2);
        assertThrows(IllegalOperationException.class, () -> serviceService.delete(1L));
    }

}
