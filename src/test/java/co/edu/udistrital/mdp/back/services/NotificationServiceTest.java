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
        u1.setName("alice");

        u2 = new UserEntity();
        u2.setId(2L);
        u2.setName("bob");
    }

    @Test
    @DisplayName("createNotification: mensaje nulo o vacío -> IllegalArgumentException")
    void createNotification_emptyMessage_throws() {
        List<Long> userIds = List.of(1L);
        
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> service.createNotification(null, userIds));
        assertNotNull(ex1);

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> service.createNotification("   ", userIds));
        assertNotNull(ex2);

        verifyNoInteractions(userRepository, notificationRepository);
    }

    @Test
    @DisplayName("createNotification: sin usuarios encontrados -> EntityNotFoundException")
    void createNotification_noUsers_throws() {
        List<Long> ids = List.of(99L);
        List<UserEntity> emptyList = List.of();

        when(userRepository.findAllById(ids)).thenReturn(emptyList);

        assertThrows(EntityNotFoundException.class,
                () -> service.createNotification("Hi!", ids));

        verify(userRepository).findAllById(ids);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    @DisplayName("createNotification: OK guarda con fecha, read = false y usuarios asignados")
    void createNotification_ok() throws Exception {
        List<Long> userIds = List.of(1L, 2L);
        List<UserEntity> users = List.of(u1, u2);
        
        when(userRepository.findAllById(userIds)).thenReturn(users);
        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NotificationEntity saved = service.createNotification("Nuevo aviso", userIds);

        assertNotNull(saved);
        assertEquals("Nuevo aviso", saved.getMessage());
        assertNotNull(saved.getDate());
        assertTrue(saved.getDate() instanceof Date);
        assertEquals(Boolean.FALSE, saved.getRead());
        assertEquals(users, saved.getUsers());

        verify(userRepository).findAllById(userIds);
        verify(notificationRepository).save(any(NotificationEntity.class));
    }

    @Test
    @DisplayName("markAsRead: notificación no existe -> EntityNotFoundException")
    void markAsRead_notFound() {
        Long notificationId = 123L;
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, 
                () -> service.markAsRead(notificationId));
        assertNotNull(thrown);

        verify(notificationRepository).findById(notificationId);
        verifyNoMoreInteractions(notificationRepository);
    }

    @Test
    @DisplayName("markAsRead: OK cambia read a true y guarda")
    void markAsRead_ok() throws Exception {
        Long notificationId = 10L;
        NotificationEntity n = new NotificationEntity();
        n.setId(notificationId);
        n.setMessage("Ping");
        n.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.markAsRead(notificationId);

        assertEquals(Boolean.TRUE, n.getRead());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(n);
    }

    @Test
    @DisplayName("getUserNotifications: onlyUnread=true usa findByUserIdAndReadFalse")
    void getUserNotifications_onlyUnread() {
        Long userId = 1L;
        List<NotificationEntity> emptyList = List.of();
        
        when(notificationRepository.findByUsers_IdAndReadFalse(userId)).thenReturn(emptyList);

        List<NotificationEntity> result = service.getUserNotifications(userId, true);

        assertNotNull(result);
        verify(notificationRepository).findByUsers_IdAndReadFalse(userId);
        verifyNoMoreInteractions(notificationRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserNotifications: onlyUnread=false usa findByUserId")
    void getUserNotifications_all() {
        Long userId = 1L;
        List<NotificationEntity> emptyList = List.of();
        
        when(notificationRepository.findByUsers_Id(userId)).thenReturn(emptyList);

        List<NotificationEntity> result = service.getUserNotifications(userId, false);

        assertNotNull(result);
        verify(notificationRepository).findByUsers_Id(userId);
        verifyNoMoreInteractions(notificationRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("deleteNotification: no existe -> EntityNotFoundException")
    void deleteNotification_notFound() {
        Long notificationId = 77L;
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, 
                () -> service.deleteNotification(notificationId));
        assertNotNull(thrown);

        verify(notificationRepository).findById(notificationId);
        verifyNoMoreInteractions(notificationRepository);
    }

    @Test
    @DisplayName("deleteNotification: no se puede eliminar si no está leída -> IllegalArgumentException")
    void deleteNotification_unread_forbidden() {
        Long notificationId = 11L;
        NotificationEntity n = new NotificationEntity();
        n.setId(notificationId);
        n.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(n));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, 
                () -> service.deleteNotification(notificationId));
        assertNotNull(thrown);

        verify(notificationRepository).findById(notificationId);
        verifyNoMoreInteractions(notificationRepository);
    }

    @Test
    @DisplayName("deleteNotification: leída -> elimina")
    void deleteNotification_read_ok() throws Exception {
        Long notificationId = 12L;
        NotificationEntity n = new NotificationEntity();
        n.setId(notificationId);
        n.setRead(true);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(n));

        service.deleteNotification(notificationId);

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).delete(n);
    }

    @Test
    @DisplayName("updateMessage lanza IllegalArgumentException si mensaje es vacío")
    void updateMessage_empty_throwsException() {
        Long notificationId = 1L;
        String emptyMessage = "  ";
        
        assertThrows(IllegalArgumentException.class,
                () -> service.updateMessage(notificationId, emptyMessage));
    }
}