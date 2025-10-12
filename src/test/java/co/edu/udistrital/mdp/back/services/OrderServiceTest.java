package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.entities.OrderEntity;
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
import static org.mockito.Mockito.*;

@SpringBootTest(classes = OrderService.class) // carga solo el bean del servicio
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @MockBean private OrderRepository orderRepository;
    @MockBean private ProductRepository productRepository;
    @MockBean private OrderDetailRepository orderDetailRepository;
    @MockBean private PersonRepository personRepository; // requerido por el constructor/autowiring

    private OrderEntity order;
    private ProductEntity product;
    private OrderDetailEntity detail;

    @BeforeEach
    void setUp() {
        // Producto
        product = new ProductEntity();
        product.setId(1L);
        product.setName("Dog Shampoo");
        product.setStock(10);
        product.setPrice(25.0);

        // Detalle
        detail = new OrderDetailEntity();
        // asumiendo setters estándar:
        detail.setProduct(product);
        detail.setQuantity(3);

        // Orden
        order = new OrderEntity();
        order.setId(100L);
        order.setStatus("CREATED");
        order.setOrderDetails(new ArrayList<>(List.of(detail)));
    }

    // -------- changeStatus --------

    @Test
    @DisplayName("changeStatus: orden no existe -> EntityNotFoundException")
    void changeStatus_notFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> orderService.changeStatus(999L, "CONFIRMED"));

        verify(orderRepository).findById(999L);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus: si la orden está PAID no se puede cambiar -> IllegalOperationException")
    void changeStatus_fromPaid_forbidden() {
        order.setStatus("PAID");
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(100L, "CANCELLED"));

        verify(orderRepository).findById(100L);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus: si la orden está CANCELLED no se puede cambiar -> IllegalOperationException")
    void changeStatus_fromCancelled_forbidden() {
        order.setStatus("CANCELLED");
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(100L, "CONFIRMED"));

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
                () -> orderService.changeStatus(100L, "CONFIRMED"));

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

        OrderEntity updated = orderService.changeStatus(100L, "CONFIRMED");

        assertEquals("CONFIRMED", updated.getStatus());
        verify(orderRepository).findById(100L);
        verify(orderDetailRepository).countReservedForProduct(1L);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository, orderDetailRepository, productRepository);
    }

    @Test
    @DisplayName("changeStatus -> PAID: descuenta stock por cada ítem y persiste productos, luego guarda orden")
    void changeStatus_paid_ok() throws Exception {
        order.setStatus("CONFIRMED"); // desde estado permitido
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(100L, "PAID");

        assertEquals("PAID", updated.getStatus());
        assertEquals(7, product.getStock()); // 10 - 3
        verify(orderRepository).findById(100L);
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> CANCELLED: solo cambia estado y guarda la orden")
    void changeStatus_cancelled_ok() throws Exception {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(100L, "CANCELLED");

        assertEquals("CANCELLED", updated.getStatus());
        verify(orderRepository).findById(100L);
        verify(orderRepository).save(order);
        verifyNoInteractions(productRepository);
        verifyNoMoreInteractions(orderRepository, orderDetailRepository);
    }

    @Test
    @DisplayName("changeStatus -> estado desconocido: asigna tal cual y guarda")
    void changeStatus_unknownStatus_setsDirectly() throws Exception {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(100L, "ON_HOLD");

        assertEquals("ON_HOLD", updated.getStatus());
        verify(orderRepository).findById(100L);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository, productRepository, orderDetailRepository);
    }

    // -------- updateOrder --------

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

        order.setStatus("PAID");
        assertThrows(IllegalOperationException.class,
                () -> orderService.updateOrder(100L, new OrderEntity()));

        order.setStatus("CANCELLED");
        assertThrows(IllegalOperationException.class,
                () -> orderService.updateOrder(100L, new OrderEntity()));

        verify(orderRepository, times(2)).findById(100L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("updateOrder: estado permitido -> guarda la orden")
    void updateOrder_allowed_saves() throws Exception {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        order.setStatus("CREATED");
        OrderEntity saved = orderService.updateOrder(100L, order);

        assertNotNull(saved);
        verify(orderRepository).findById(100L);
        verify(orderRepository).save(order);
        verifyNoMoreInteractions(orderRepository);
    }

    // -------- deleteOrder --------

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
        order.setStatus("PAID");
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(IllegalOperationException.class, () -> orderService.deleteOrder(100L));

        verify(orderRepository).findById(100L);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("deleteOrder: estado no PAID -> elimina")
    void deleteOrder_allowed_deletes() throws Exception {
        order.setStatus("CANCELLED"); // permitido por la regla (no PAID)
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(100L);

        verify(orderRepository).findById(100L);
        verify(orderRepository).delete(order);
        verifyNoMoreInteractions(orderRepository);
    }
}

