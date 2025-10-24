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
        Long invalidOrderId = 999L;
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        OrderDetailEntity newDetail = new OrderDetailEntity();
        newDetail.setQuantity(1);
        newDetail.setProduct(product); 

        assertThrows(EntityNotFoundException.class,
                () -> service.addOrderDetail(invalidOrderId, newDetail));

        verify(orderRepository).findById(invalidOrderId);
        verifyNoInteractions(orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("addOrderDetail: no se puede agregar a una orden PAID")
    void addOrderDetail_paidForbidden() {
        Long paidOrderId = 12L;
        when(orderRepository.findById(paidOrderId)).thenReturn(Optional.of(orderPaid));

        OrderDetailEntity newDetail = new OrderDetailEntity();
        newDetail.setQuantity(1);
        newDetail.setProduct(product);

        assertThrows(IllegalOperationException.class,
                () -> service.addOrderDetail(paidOrderId, newDetail));

        verify(orderRepository).findById(paidOrderId);
        verifyNoInteractions(orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("addOrderDetail: no se puede agregar a una orden CANCELLED")
    void addOrderDetail_cancelledForbidden() {
        Long cancelledOrderId = 13L;
        when(orderRepository.findById(cancelledOrderId)).thenReturn(Optional.of(orderCancelled));

        OrderDetailEntity newDetail = new OrderDetailEntity();
        newDetail.setQuantity(1);
        newDetail.setProduct(product);

        assertThrows(IllegalOperationException.class,
                () -> service.addOrderDetail(cancelledOrderId, newDetail));

        verify(orderRepository).findById(cancelledOrderId);
        verifyNoInteractions(orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("addOrderDetail: quantity <= 0 -> IllegalOperationException")
    void addOrderDetail_invalidQuantity() {
        Long pendingOrderId = 10L;
        when(orderRepository.findById(pendingOrderId)).thenReturn(Optional.of(orderPending));

        OrderDetailEntity newDetail = new OrderDetailEntity();
        newDetail.setQuantity(0);
        newDetail.setProduct(product);

        assertThrows(IllegalOperationException.class,
                () -> service.addOrderDetail(pendingOrderId, newDetail));

        verify(orderRepository).findById(pendingOrderId);
        verifyNoInteractions(orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("addOrderDetail: OK calcula subtotal (price * quantity) y guarda; persiste recálculo de la orden")
    void addOrderDetail_ok() throws Exception {
        Long pendingOrderId = 10L;
        Long productId = 1L;
        
        when(orderRepository.findById(pendingOrderId)).thenReturn(Optional.of(orderPending));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderDetailRepository.save(any(OrderDetailEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDetailEntity newDetail = new OrderDetailEntity();
        newDetail.setQuantity(3);
        newDetail.setProduct(product);

        OrderDetailEntity saved = service.addOrderDetail(pendingOrderId, newDetail);

        assertSame(orderPending, saved.getOrder());
        assertEquals(3, saved.getQuantity());
        assertEquals(75.0, saved.getSubtotal());

        verify(orderRepository).findById(pendingOrderId);
        verify(productRepository).findById(productId);
        verify(orderDetailRepository).save(newDetail);
        verify(orderRepository).save(orderPending);
    }


    @Test
    @DisplayName("updateOrderDetail: detalle no existe -> EntityNotFoundException")
    void updateOrderDetail_notFound() {
        Long detailId = 100L;
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.empty());

        OrderDetailEntity updateDetail = new OrderDetailEntity();

        assertThrows(EntityNotFoundException.class,
                () -> service.updateOrderDetail(detailId, updateDetail));

        verify(orderDetailRepository).findById(detailId);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(orderRepository, productRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: orden PAID o CANCELLED -> IllegalOperationException")
    void updateOrderDetail_forbiddenStates() {
        Long detailId = 100L;
        
        existingDetail.setOrder(orderPaid);
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.of(existingDetail));
        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(detailId, existingDetail));

        existingDetail.setOrder(orderCancelled);
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.of(existingDetail));
        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(detailId, existingDetail));

        verify(orderDetailRepository, times(2)).findById(detailId);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: quantity <= 0 -> IllegalOperationException")
    void updateOrderDetail_invalidQuantity() {
        Long detailId = 100L;
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.of(existingDetail));

        OrderDetailEntity incoming = new OrderDetailEntity();
        incoming.setQuantity(0);

        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(detailId, incoming));

        verify(orderDetailRepository).findById(detailId);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: CONFIRMED -> no se permiten cambios (solo PENDING)")
    void updateOrderDetail_confirmed_forbidden() {
        Long detailId = 100L;
        
        existingDetail.setOrder(orderConfirmed);
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.of(existingDetail));

        OrderDetailEntity incoming = new OrderDetailEntity();
        incoming.setQuantity(5);

        assertThrows(IllegalOperationException.class,
                () -> service.updateOrderDetail(detailId, incoming));

        verify(orderDetailRepository).findById(detailId);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("updateOrderDetail: PENDING permite cambios; recalcula subtotal (price * quantity) y guarda")
    void updateOrderDetail_pending_allowsAndRecalculates() throws Exception {
        Long detailId = 100L;
        Long productId = 1L;
        
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.of(existingDetail));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderDetailRepository.save(any(OrderDetailEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDetailEntity incoming = new OrderDetailEntity();
        incoming.setQuantity(9);
        incoming.setProduct(product); 

        OrderDetailEntity saved = service.updateOrderDetail(detailId, incoming);

        assertEquals(9, saved.getQuantity());
        assertEquals(25.0 * 9, saved.getSubtotal());
        verify(orderDetailRepository).findById(detailId);
        verify(productRepository).findById(productId);
        verify(orderDetailRepository).save(existingDetail);
        verify(orderRepository).save(orderPending); 
    }


    @Test
    @DisplayName("deleteOrderDetail: detalle no existe -> EntityNotFoundException")
    void deleteOrderDetail_notFound() {
        Long detailId = 100L;
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.deleteOrderDetail(detailId));

        verify(orderDetailRepository).findById(detailId);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("deleteOrderDetail: orden PAID o CANCELLED -> IllegalOperationException")
    void deleteOrderDetail_forbiddenStates() {
        Long detailId = 100L;
        
        existingDetail.setOrder(orderPaid);
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.of(existingDetail));
        assertThrows(IllegalOperationException.class,
                () -> service.deleteOrderDetail(detailId));

        existingDetail.setOrder(orderCancelled);
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.of(existingDetail));
        assertThrows(IllegalOperationException.class,
                () -> service.deleteOrderDetail(detailId));

        verify(orderDetailRepository, times(2)).findById(detailId);
        verifyNoMoreInteractions(orderDetailRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("deleteOrderDetail: PENDING -> elimina y persiste recálculo de la orden")
    void deleteOrderDetail_ok() throws Exception {
        Long detailId = 100L;
        when(orderDetailRepository.findById(detailId)).thenReturn(Optional.of(existingDetail));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deleteOrderDetail(detailId);

        verify(orderDetailRepository).findById(detailId);
        verify(orderDetailRepository).delete(existingDetail);
        verify(orderRepository).save(orderPending);
    }
}