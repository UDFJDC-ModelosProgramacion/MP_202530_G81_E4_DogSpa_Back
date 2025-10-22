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
        order.setStatus(OrderStatus.PENDING); // antes "CREATED"
        order.setOrderDetails(new ArrayList<>(List.of(detail)));
    }

    @Test
    @DisplayName("changeStatus: orden no existe -> EntityNotFoundException")
    void changeStatus_notFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> orderService.changeStatus(999L, OrderStatus.CONFIRMED));

        verify(orderRepository).findById(999L);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus: desde CANCELLED no se puede cambiar -> IllegalOperationException")
    void changeStatus_fromCancelled_forbidden() {
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(100L, OrderStatus.CONFIRMED));

        verify(orderRepository).findById(100L);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> CONFIRMED: falla si disponible < cantidad (stock - reservado)")
    void changeStatus_confirmed_notEnoughStock() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        // reservado = 8 -> disponible = 10 - 8 = 2 < 3 -> debe fallar
        when(orderDetailRepository.countReservedForProduct(1L)).thenReturn(8);

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(100L, OrderStatus.CONFIRMED));

        verify(orderRepository).findById(100L);
        verify(orderDetailRepository).countReservedForProduct(1L);
        verifyNoMoreInteractions(orderRepository, orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("changeStatus -> CONFIRMED: OK cuando hay stock suficiente")
    void changeStatus_confirmed_ok() throws Exception {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderDetailRepository.countReservedForProduct(1L)).thenReturn(2); // disponible = 8 >= 3
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(100L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
        verify(orderRepository).findById(100L);
        verify(orderDetailRepository).countReservedForProduct(1L);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository, orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("changeStatus -> PAID: requiere CONFIRMED; si está PENDING lanza IllegalOperationException")
    void changeStatus_paid_requiresConfirmed() {
        order.setStatus(OrderStatus.PENDING); 
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(100L, OrderStatus.PAID));

        verify(orderRepository).findById(100L);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> PAID: descuenta stock por cada ítem y persiste productos, luego guarda orden")
    void changeStatus_paid_ok() throws Exception {
        order.setStatus(OrderStatus.CONFIRMED); // transición válida CONFIRMED -> PAID
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(100L, OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, updated.getStatus());
        assertEquals(7, product.getStock()); // 10 - 3
        verify(orderRepository).findById(100L);
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> desde PAID a SHIPPED: permitido")
    void changeStatus_paid_to_shipped_ok() throws Exception {
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(100L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, updated.getStatus());
        verify(orderRepository).findById(100L);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> transición inválida: PENDING a SHIPPED lanza IllegalOperationException")
    void changeStatus_invalidTransition_forbidden() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(100L, OrderStatus.SHIPPED));

        verify(orderRepository).findById(100L);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("updateOrder: orden no existe -> EntityNotFoundException")
    void updateOrder_notFound() {
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> orderService.updateOrder(100L, new OrderEntity()));
        verify(orderRepository).findById(100L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("updateOrder: si está PAID o CANCELLED -> IllegalOperationException")
    void updateOrder_forbiddenStates() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        order.setStatus(OrderStatus.PAID);
        assertThrows(IllegalOperationException.class,
                () -> orderService.updateOrder(100L, new OrderEntity()));

        order.setStatus(OrderStatus.CANCELLED);
        assertThrows(IllegalOperationException.class,
                () -> orderService.updateOrder(100L, new OrderEntity()));

        verify(orderRepository, times(2)).findById(100L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("updateOrder: estado permitido (PENDING) -> guarda la orden")
    void updateOrder_allowed_saves() throws Exception {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        order.setStatus(OrderStatus.PENDING);
        OrderEntity saved = orderService.updateOrder(100L, order);

        assertNotNull(saved);
        verify(orderRepository).findById(100L);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("deleteOrder: orden no existe -> EntityNotFoundException")
    void deleteOrder_notFound() {
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.deleteOrder(100L));
        verify(orderRepository).findById(100L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("deleteOrder: si está PAID -> IllegalOperationException")
    void deleteOrder_paid_forbidden() {
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class, () -> orderService.deleteOrder(100L));

        verify(orderRepository).findById(100L);
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
