package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.entities.OrderEntity;
import co.edu.udistrital.mdp.back.entities.OrderStatus;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.OrderRepository;
import co.edu.udistrital.mdp.back.repositories.PersonRepository;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = OrderService.class)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @MockBean private OrderRepository orderRepository;
    @MockBean private ProductRepository productRepository;
    @MockBean private OrderDetailRepository orderDetailRepository;
    @MockBean private PersonRepository personRepository; 

    private OrderEntity order;
    private ProductEntity product;
    private OrderDetailEntity detail;

    @BeforeEach
    void setUp() {
       
        product = new ProductEntity();
        product.setId(1L);
        product.setName("Dog Shampoo");
        product.setStock(10);
        product.setPrice(25.0);

        detail = new OrderDetailEntity();
        detail.setProduct(product);
        detail.setQuantity(3);

        order = new OrderEntity();
        order.setId(100L);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDetails(new ArrayList<>(List.of(detail)));
    }

    @Test
    @DisplayName("changeStatus: orden no existe -> EntityNotFoundException")
    void changeStatus_notFound() {
        Long orderId = 999L;
        OrderStatus newStatus = OrderStatus.CONFIRMED;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> orderService.changeStatus(orderId, newStatus));

        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus: desde CANCELLED no se puede cambiar -> IllegalOperationException")
    void changeStatus_fromCancelled_forbidden() {
        Long orderId = 100L;
        OrderStatus newStatus = OrderStatus.CONFIRMED;
        
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(orderId, newStatus));

        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> CONFIRMED: falla si disponible < cantidad (stock - reservado)")
    void changeStatus_confirmed_notEnoughStock() {
        Long orderId = 100L;
        Long productId = 1L;
        OrderStatus newStatus = OrderStatus.CONFIRMED;
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderDetailRepository.countReservedForProduct(productId)).thenReturn(8);

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(orderId, newStatus));

        verify(orderRepository).findById(orderId);
        verify(orderDetailRepository).countReservedForProduct(productId);
        verifyNoMoreInteractions(orderRepository, orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("changeStatus -> CONFIRMED: OK cuando hay stock suficiente")
    void changeStatus_confirmed_ok() throws Exception {
        Long orderId = 100L;
        Long productId = 1L;
        OrderStatus newStatus = OrderStatus.CONFIRMED;
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderDetailRepository.countReservedForProduct(productId)).thenReturn(2);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(orderId, newStatus);

        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
        verify(orderRepository).findById(orderId);
        verify(orderDetailRepository).countReservedForProduct(productId);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository, orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("changeStatus -> PAID: requiere CONFIRMED; si está PENDING lanza IllegalOperationException")
    void changeStatus_paid_requiresConfirmed() {
        Long orderId = 100L;
        OrderStatus newStatus = OrderStatus.PAID;
        
        order.setStatus(OrderStatus.PENDING); 
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(orderId, newStatus));

        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> PAID: descuenta stock por cada ítem y persiste productos, luego guarda orden")
    void changeStatus_paid_ok() throws Exception {
        Long orderId = 100L;
        OrderStatus newStatus = OrderStatus.PAID;
        
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(orderId, newStatus);

        assertEquals(OrderStatus.PAID, updated.getStatus());
        assertEquals(7, product.getStock());
        verify(orderRepository).findById(orderId);
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> desde PAID a SHIPPED: permitido")
    void changeStatus_paid_to_shipped_ok() throws Exception {
        Long orderId = 100L;
        OrderStatus newStatus = OrderStatus.SHIPPED;
        
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(orderId, newStatus);

        assertEquals(OrderStatus.SHIPPED, updated.getStatus());
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> transición inválida: PENDING a SHIPPED lanza IllegalOperationException")
    void changeStatus_invalidTransition_forbidden() {
        Long orderId = 100L;
        OrderStatus newStatus = OrderStatus.SHIPPED;
        
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(orderId, newStatus));

        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("updateOrder: orden no existe -> EntityNotFoundException")
    void updateOrder_notFound() {
        Long orderId = 100L;
        OrderEntity updateData = new OrderEntity();
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class,
                () -> orderService.updateOrder(orderId, updateData));
        
        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("updateOrder: si está PAID o CANCELLED -> IllegalOperationException")
    void updateOrder_forbiddenStates() {
        Long orderId = 100L;
        OrderEntity updateData = new OrderEntity();
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        order.setStatus(OrderStatus.PAID);
        assertThrows(IllegalOperationException.class,
                () -> orderService.updateOrder(orderId, updateData));

        order.setStatus(OrderStatus.CANCELLED);
        assertThrows(IllegalOperationException.class,
                () -> orderService.updateOrder(orderId, updateData));

        verify(orderRepository, times(2)).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("updateOrder: estado permitido (PENDING) -> guarda la orden")
    void updateOrder_allowed_saves() throws Exception {
        Long orderId = 100L;
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        order.setStatus(OrderStatus.PENDING);
        OrderEntity saved = orderService.updateOrder(orderId, order);

        assertNotNull(saved);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("deleteOrder: orden no existe -> EntityNotFoundException")
    void deleteOrder_notFound() {
        Long orderId = 100L;
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class, 
                () -> orderService.deleteOrder(orderId));
        
        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("deleteOrder: si está PAID -> IllegalOperationException")
    void deleteOrder_paid_forbidden() {
        Long orderId = 100L;
        
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class, 
                () -> orderService.deleteOrder(orderId));

        verify(orderRepository).findById(orderId);
        verifyNoMoreInteractions(orderRepository);
    }
    @Test
    @DisplayName("changeStatus lanza IllegalOperationException si la transición es inválida")
    void changeStatus_invalidTransition_throwsException() throws Exception {
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class,
            () -> orderService.changeStatus(1L, OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("deleteOrder lanza IllegalOperationException si el pedido ya está completado")
    void deleteOrder_immutableStatus_throwsException() {
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        assertThrows(IllegalOperationException.class, () -> orderService.deleteOrder(1L));
    }

    @Test
    @DisplayName("updateOrder lanza EntityNotFoundException si no existe el pedido")
    void updateOrder_notFound_throwsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, 
            () -> orderService.updateOrder(1L, new OrderEntity()));
    }

}