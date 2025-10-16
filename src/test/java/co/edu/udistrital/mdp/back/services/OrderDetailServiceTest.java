package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.entities.OrderEntity;
import co.edu.udistrital.mdp.back.entities.OrderStatus;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.OrderRepository;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    @MockBean private ProductRepository productRepository;

    private OrderEntity orderPending;
    private OrderEntity orderConfirmed;
    private OrderEntity orderPaid;
    private OrderEntity orderCancelled;

    private ProductEntity product;
    private OrderDetailEntity existingDetail; 

    @BeforeEach
    void setUp() {
        
        product = new ProductEntity();
        product.setId(1L);
        product.setName("Dog Shampoo");
        product.setPrice(25.0);

        orderPending = new OrderEntity();
        orderPending.setId(10L);
        orderPending.setStatus(OrderStatus.PENDING);
        orderPending.setDiscount(0.0);

        orderConfirmed = new OrderEntity();
        orderConfirmed.setId(11L);
        orderConfirmed.setStatus(OrderStatus.CONFIRMED);
        orderConfirmed.setDiscount(0.0);

        orderPaid = new OrderEntity();
        orderPaid.setId(12L);
        orderPaid.setStatus(OrderStatus.PAID);
        orderPaid.setDiscount(0.0);

        orderCancelled = new OrderEntity();
        orderCancelled.setId(13L);
        orderCancelled.setStatus(OrderStatus.CANCELLED);
        orderCancelled.setDiscount(0.0);

        existingDetail = new OrderDetailEntity();
        existingDetail.setId(100L);
        existingDetail.setOrder(orderPending);
        existingDetail.setProduct(product);
        existingDetail.setQuantity(2);
        existingDetail.setSubtotal(50.0); 
        orderPending.getOrderDetails().add(existingDetail);
    }

    @Test
    @DisplayName("addOrderDetail: orden no existe -> EntityNotFoundException")
    void addOrderDetail_notFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(1);
        newDetail.setProduct(product); 

        assertThrows(EntityNotFoundException.class,
                () -> service.addOrderDetail(999L, newDetail));

        verify(orderRepository).findById(999L);
        verifyNoInteractions(orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("addOrderDetail: no se puede agregar a una orden PAID")
    void addOrderDetail_paidForbidden() {
        when(orderRepository.findById(12L)).thenReturn(Optional.of(orderPaid));

        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(1);
        newDetail.setProduct(product);

        assertThrows(IllegalOperationException.class,
                () -> service.addOrderDetail(12L, newDetail));

        verify(orderRepository).findById(12L);
        verifyNoInteractions(orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("addOrderDetail: no se puede agregar a una orden CANCELLED")
    void addOrderDetail_cancelledForbidden() {
        when(orderRepository.findById(13L)).thenReturn(Optional.of(orderCancelled));

        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(1);
        newDetail.setProduct(product);

        assertThrows(IllegalOperationException.class,
                () -> service.addOrderDetail(13L, newDetail));

        verify(orderRepository).findById(13L);
        verifyNoInteractions(orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("addOrderDetail: quantity <= 0 -> IllegalOperationException")
    void addOrderDetail_invalidQuantity() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(orderPending));

        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(0);
        newDetail.setProduct(product);

        assertThrows(IllegalOperationException.class,
                () -> service.addOrderDetail(10L, newDetail));

        verify(orderRepository).findById(10L);
        verifyNoInteractions(orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("addOrderDetail: OK calcula subtotal (price * quantity) y guarda; persiste recálculo de la orden")
    void addOrderDetail_ok() throws Exception {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(orderPending));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderDetailRepository.save(any(OrderDetailEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var newDetail = new OrderDetailEntity();
        newDetail.setQuantity(3);
        newDetail.setProduct(product);

        var saved = service.addOrderDetail(10L, newDetail);

        assertSame(orderPending, saved.getOrder());
        assertEquals(3, saved.getQuantity());
        assertEquals(75.0, saved.getSubtotal());

        verify(orderRepository).findById(10L);
        verify(productRepository).findById(1L);
        verify(orderDetailRepository).save(newDetail);
        verify(orderRepository).save(orderPending);
    }


    @Test
    @DisplayName("updateOrderDetail: detalle no existe -> EntityNotFoundException")
    void updateOrderDetail_notFound() {
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateOrderDetail(100L, new OrderDetailEntity()));

        verify(orderDetailRepository).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(orderRepository, productRepository);
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
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: quantity <= 0 -> IllegalOperationException")
    void updateOrderDetail_invalidQuantity() {
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));

        var incoming = new OrderDetailEntity();
        incoming.setQuantity(0);

        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(100L, incoming));

        verify(orderDetailRepository).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: CONFIRMED -> no se permiten cambios (solo PENDING)")
    void updateOrderDetail_confirmed_forbidden() {
        existingDetail.setOrder(orderConfirmed);
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));

        var incoming = new OrderDetailEntity();
        incoming.setQuantity(5);

        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(100L, incoming));

        verify(orderDetailRepository).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: PENDING permite cambios; recalcula subtotal (price * quantity) y guarda")
    void updateOrderDetail_pending_allowsAndRecalculates() throws Exception {
        // Detalle asociado a PENDING (ya en setUp)
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderDetailRepository.save(any(OrderDetailEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var incoming = new OrderDetailEntity();
        incoming.setQuantity(9);
        incoming.setProduct(product); 

        var saved = service.updateOrderDetail(100L, incoming);

        assertEquals(9, saved.getQuantity());
        assertEquals(25.0 * 9, saved.getSubtotal());
        verify(orderDetailRepository).findById(100L);
        verify(productRepository).findById(1L);
        verify(orderDetailRepository).save(existingDetail);
        verify(orderRepository).save(orderPending); 
    }


    @Test
    @DisplayName("deleteOrderDetail: detalle no existe -> EntityNotFoundException")
    void deleteOrderDetail_notFound() {
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.deleteOrderDetail(100L));

        verify(orderDetailRepository).findById(100L);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(orderRepository);
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
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("deleteOrderDetail: PENDING -> elimina y persiste recálculo de la orden")
    void deleteOrderDetail_ok() throws Exception {
        when(orderDetailRepository.findById(100L)).thenReturn(Optional.of(existingDetail));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deleteOrderDetail(100L);

        verify(orderDetailRepository).findById(100L);
        verify(orderDetailRepository).delete(existingDetail);
        verify(orderRepository).save(orderPending); // recálculo de total
    }
}
