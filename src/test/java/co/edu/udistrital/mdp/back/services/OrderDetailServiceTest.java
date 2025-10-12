package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.entities.OrderEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.OrderRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = OrderDetailService.class)
class OrderDetailServiceTest {

    @Autowired
    private OrderDetailService service;

    @MockBean private OrderDetailRepository orderDetailRepository;
    @MockBean private OrderRepository orderRepository;

    private OrderEntity orderCreated;
    private OrderEntity orderConfirmed;
    private OrderEntity orderPaid;
    private OrderEntity orderCancelled;

    private OrderDetailEntity existingDetail; // para update/delete

    @BeforeEach
    void setUp() {
        orderCreated = new OrderEntity();
        orderCreated.setId(10L);
        orderCreated.setStatus("CREATED");

        orderConfirmed = new OrderEntity();
        orderConfirmed.setId(11L);
        orderConfirmed.setStatus("CONFIRMED");

        orderPaid = new OrderEntity();
        orderPaid.setId(12L);
        orderPaid.setStatus("PAID");

        orderCancelled = new OrderEntity();
        orderCancelled.setId(13L);
        orderCancelled.setStatus("CANCELLED");

        existingDetail = new OrderDetailEntity();
        existingDetail.setId(100L);
        existingDetail.setOrder(orderCreated);
        existingDetail.setQuantity(2);
        existingDetail.setSubtotal(50.0);
    }

    // -------------------- addOrderDetail --------------------

    @Test
    @DisplayName("addOrderDetail: orden no existe -> EntityNotFoundException")
    void addOrderDetail_notFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(1);
        newDetail.setSubtotal(10.0);

        assertThrows(EntityNotFoundException.class,
                () -> service.addOrderDetail(999L, newDetail));

        verify(orderRepository).findById(999L);
        verifyNoInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("addOrderDetail: no se puede agregar a una orden PAID")
    void addOrderDetail_paidForbidden() {
        when(orderRepository.findById(12L)).thenReturn(Optional.of(orderPaid));
        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(1);
        newDetail.setSubtotal(10.0);

        assertThrows(IllegalOperationException.class,
                () -> service.addOrderDetail(12L, newDetail));

        verify(orderRepository).findById(12L);
        verifyNoInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("addOrderDetail: no se puede agregar a una orden CANCELLED")
    void addOrderDetail_cancelledForbidden() {
        when(orderRepository.findById(13L)).thenReturn(Optional.of(orderCancelled));
        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(1);
        newDetail.setSubtotal(10.0);

        assertThrows(IllegalOperationException.class,
                () -> service.addOrderDetail(13L, newDetail));

        verify(orderRepository).findById(13L);
        verifyNoInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("addOrderDetail: quantity <= 0 -> IllegalOperationException")
    void addOrderDetail_invalidQuantity() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(orderCreated));
        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(0);
        newDetail.setSubtotal(10.0);

        assertThrows(IllegalOperationException.class,
                () -> service.addOrderDetail(10L, newDetail));

        verify(orderRepository).findById(10L);
        verifyNoInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("addOrderDetail: OK asigna la orden y guarda el detalle")
    void addOrderDetail_ok() throws Exception {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(orderCreated));
        when(orderDetailRepository.save(any(OrderDetailEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(3);
        newDetail.setSubtotal(30.0);

        var saved = service.addOrderDetail(10L, newDetail);

        assertSame(orderCreated, newDetail.getOrder());
        assertSame(orderCreated, saved.getOrder());
        assertEquals(3, saved.getQuantity());
        assertEquals(30.0, saved.getSubtotal());

        verify(orderRepository).findById(10L);
        verify(orderDetailRepository).save(newDetail);
    }

    // -------------------- updateOrderDetail --------------------

    @Test
    @DisplayName("updateOrderDetail: detalle no existe -> EntityNotFoundException")
    void updateOrderDetail_notFound() {
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateOrderDetail(100L, new OrderDetailEntity()));

        verify(orderDetailRepository).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: orden PAID o CANCELLED -> IllegalOperationException")
    void updateOrderDetail_forbiddenStates() {
        existingDetail.setOrder(orderPaid);
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));
        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(100L, existingDetail));

        existingDetail.setOrder(orderCancelled);
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));
        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(100L, existingDetail));

        verify(orderDetailRepository, times(2)).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: quantity <= 0 -> IllegalOperationException")
    void updateOrderDetail_invalidQuantity() {
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));

        var incoming = new OrderDetailEntity();
        incoming.setQuantity(0);
        incoming.setSubtotal(999.0);

        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(100L, incoming));

        verify(orderDetailRepository).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: CONFIRMED y cambia subtotal -> IllegalOperationException")
    void updateOrderDetail_confirmedChangingPrice_forbidden() {
        existingDetail.setOrder(orderConfirmed);
        existingDetail.setSubtotal(50.0);

        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));

        var incoming = new OrderDetailEntity();
        incoming.setQuantity(5);
        incoming.setSubtotal(60.0); // distinto

        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(100L, incoming));

        verify(orderDetailRepository).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: CONFIRMED con el mismo subtotal -> actualiza solo cantidad")
    void updateOrderDetail_confirmedSamePrice_updatesQuantityOnly() throws Exception {
        existingDetail.setOrder(orderConfirmed);
        existingDetail.setSubtotal(50.0);
        existingDetail.setQuantity(2);

        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));
        when(orderDetailRepository.save(any(OrderDetailEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var incoming = new OrderDetailEntity();
        incoming.setQuantity(7);
        incoming.setSubtotal(50.0); // igual

        var saved = service.updateOrderDetail(100L, incoming);

        assertEquals(7, saved.getQuantity());
        assertEquals(50.0, saved.getSubtotal()); // sin cambio
        verify(orderDetailRepository).findById(100L);
        verify(orderDetailRepository).save(existingDetail);
    }

    @Test
    @DisplayName("updateOrderDetail: CREATED permite cambiar cantidad y subtotal")
    void updateOrderDetail_created_allowsAll() throws Exception {
        existingDetail.setOrder(orderCreated);
        existingDetail.setSubtotal(50.0);
        existingDetail.setQuantity(2);

        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));
        when(orderDetailRepository.save(any(OrderDetailEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var incoming = new OrderDetailEntity();
        incoming.setQuantity(9);
        incoming.setSubtotal(90.0);

        var saved = service.updateOrderDetail(100L, incoming);

        assertEquals(9, saved.getQuantity());
        assertEquals(90.0, saved.getSubtotal());
        verify(orderDetailRepository).findById(100L);
        verify(orderDetailRepository).save(existingDetail);
    }

    // -------------------- deleteOrderDetail --------------------

    @Test
    @DisplayName("deleteOrderDetail: detalle no existe -> EntityNotFoundException")
    void deleteOrderDetail_notFound() {
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.deleteOrderDetail(100L));

        verify(orderDetailRepository).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("deleteOrderDetail: orden PAID o CANCELLED -> IllegalOperationException")
    void deleteOrderDetail_forbiddenStates() {
        existingDetail.setOrder(orderPaid);
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));
        assertThrows(IllegalOperationException.class,
                () -> service.deleteOrderDetail(100L));

        existingDetail.setOrder(orderCancelled);
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));
        assertThrows(IllegalOperationException.class,
                () -> service.deleteOrderDetail(100L));

        verify(orderDetailRepository, times(2)).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
    }

    @Test
    @DisplayName("deleteOrderDetail: CREATED -> elimina el detalle")
    void deleteOrderDetail_ok() throws Exception {
        existingDetail.setOrder(orderCreated);
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));

        service.deleteOrderDetail(100L);

        verify(orderDetailRepository).findById(100L);
        verify(orderDetailRepository).delete(existingDetail);
        verifyNoMoreInteractions(orderDetailRepository);
    }
}
