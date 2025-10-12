package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.NotificationEntity;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.repositories.NotificationRepository;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = NotificationService.class)
class NotificationServiceTest {

    @Autowired
    private NotificationService service;

    @MockBean private NotificationRepository notificationRepository;
    @MockBean private UserRepository userRepository;

    private UserEntity u1;
    private UserEntity u2;

    @BeforeEach
    void setUp() {
        u1 = new UserEntity();
        u1.setId(1L);
        u1.setUsername("alice");

        u2 = new UserEntity();
        u2.setId(2L);
        u2.setUsername("bob");
    }

    // -------------------- createNotification --------------------

    @Test
    @DisplayName("createNotification: mensaje nulo o vacío -> IllegalArgumentException")
    void createNotification_emptyMessage_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createNotification(null, List.of(1L)));

        assertThrows(IllegalArgumentException.class,
                () -> service.createNotification("   ", List.of(1L)));

        verifyNoInteractions(userRepository, notificationRepository);
    }

    @Test
    @DisplayName("createNotification: sin usuarios encontrados -> EntityNotFoundException")
    void createNotification_noUsers_throws() {
        when(userRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class,
                () -> service.createNotification("Hi!", List.of(99L)));

        verify(userRepository).findAllById(List.of(99L));
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    @DisplayName("createNotification: OK guarda con fecha, read = false y usuarios asignados")
    void createNotification_ok() throws Exception {
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(u1, u2));
        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NotificationEntity saved = service.createNotification("Nuevo aviso", List.of(1L, 2L));

        assertNotNull(saved);
        assertEquals("Nuevo aviso", saved.getMessage());
        assertNotNull(saved.getDate());
        assertTrue(saved.getDate() instanceof Date);
        assertEquals(Boolean.FALSE, saved.getRead());
        assertEquals(List.of(u1, u2), saved.getUser()); // el service usa setUser(List<UserEntity>)

        verify(userRepository).findAllById(List.of(1L, 2L));
        verify(notificationRepository).save(any(NotificationEntity.class));
        verifyNoMoreInteractions(userRepository, notificationRepository);
    }

    // -------------------- markAsRead --------------------

    @Test
    @DisplayName("markAsRead: notificación no existe -> EntityNotFoundException")
    void markAsRead_notFound() {
        when(notificationRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.markAsRead(123L));

        verify(notificationRepository).findById(123L);
        verifyNoMoreInteractions(notificationRepository);
    }

    @Test
    @DisplayName("markAsRead: OK cambia read a true y guarda")
    void markAsRead_ok() throws Exception {
        NotificationEntity n = new NotificationEntity();
        n.setId(10L);
        n.setMessage("Ping");
        n.setRead(false);

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.markAsRead(10L);

        assertEquals(Boolean.TRUE, n.getRead());
        verify(notificationRepository).findById(10L);
        verify(notificationRepository).save(n);
        verifyNoMoreInteractions(notificationRepository);
    }

    // -------------------- getUserNotifications --------------------

    @Test
    @DisplayName("getUserNotifications: onlyUnread=true usa findByUserIdAndReadFalse")
    void getUserNotifications_onlyUnread() {
        when(notificationRepository.findByUserIdAndReadFalse(1L)).thenReturn(List.of());

        var result = service.getUserNotifications(1L, true);

        assertNotNull(result);
        verify(notificationRepository).findByUserIdAndReadFalse(1L);
        verifyNoMoreInteractions(notificationRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserNotifications: onlyUnread=false usa findByUserId")
    void getUserNotifications_all() {
        when(notificationRepository.findByUserId(1L)).thenReturn(List.of());

        var result = service.getUserNotifications(1L, false);

        assertNotNull(result);
        verify(notificationRepository).findByUserId(1L);
        verifyNoMoreInteractions(notificationRepository);
        verifyNoInteractions(userRepository);
    }

    // -------------------- deleteNotification --------------------

    @Test
    @DisplayName("deleteNotification: no existe -> EntityNotFoundException")
    void deleteNotification_notFound() {
        when(notificationRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteNotification(77L));

        verify(notificationRepository).findById(77L);
        verifyNoMoreInteractions(notificationRepository);
    }

    @Test
    @DisplayName("deleteNotification: no se puede eliminar si no está leída -> IllegalArgumentException")
    void deleteNotification_unread_forbidden() {
        NotificationEntity n = new NotificationEntity();
        n.setId(11L);
        n.setRead(false);

        when(notificationRepository.findById(11L)).thenReturn(Optional.of(n));

        assertThrows(IllegalArgumentException.class, () -> service.deleteNotification(11L));

        verify(notificationRepository).findById(11L);
        verifyNoMoreInteractions(notificationRepository);
    }

    @Test
    @DisplayName("deleteNotification: leída -> elimina")
    void deleteNotification_read_ok() throws Exception {
        NotificationEntity n = new NotificationEntity();
        n.setId(12L);
        n.setRead(true);

        when(notificationRepository.findById(12L)).thenReturn(Optional.of(n));

        service.deleteNotification(12L);

        verify(notificationRepository).findById(12L);
        verify(notificationRepository).delete(n);
        verifyNoMoreInteractions(notificationRepository);
    }
}
