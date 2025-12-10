package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.entities.OrderEntity;
import co.edu.udistrital.mdp.back.entities.OrderStatus;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.OrderRepository;
import co.edu.udistrital.mdp.back.repositories.PersonRepository;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.lang.reflect.InvocationTargetException;
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

    @MockBean
    private OrderRepository orderRepository;
    @MockBean
    private ProductRepository productRepository;
    @MockBean
    private OrderDetailRepository orderDetailRepository;
    @MockBean
    private PersonRepository personRepository;

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
    @DisplayName("changeStatus -> CONFIRMED: OK (stock ya fue reservado en createOrder)")
    void changeStatus_confirmed_ok_noStockCheckNeeded() throws Exception {
        Long orderId = 100L;
        OrderStatus newStatus = OrderStatus.CONFIRMED;

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(orderId, newStatus);

        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
        verify(orderRepository).findById(orderId);
        // No se verifica stock aquí
        verify(orderRepository).save(order);
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
    @DisplayName("changeStatus -> PAID: cambia estado, NO descuenta stock (ya se hizo en create)")
    void changeStatus_paid_onlyChangesStatus() throws Exception {
        Long orderId = 100L;
        OrderStatus newStatus = OrderStatus.PAID;

        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(orderId, newStatus);

        assertEquals(OrderStatus.PAID, updated.getStatus());
        // El stock NO debe cambiar aquí, se mantiene en 10 (porque se asume descontado
        // en create, pero el objeto mockeado 'product' en este test específico entra
        // con 10)
        // OJO: En la realidad el objeto Order ya vendría con productos descontados.
        // Aquí solo verificamos que NO llame a productRepository.save()
        verify(orderRepository).findById(orderId);
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
    @DisplayName("changeStatus: PAID a CONFIRMED es transición inválida")
    void changeStatus_paidToConfirmed_forbidden() {
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.CONFIRMED;

        OrderEntity paidOrder = new OrderEntity();
        paidOrder.setId(orderId);
        paidOrder.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(paidOrder));

        assertThrows(IllegalOperationException.class,
                () -> orderService.changeStatus(orderId, newStatus));

        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("deleteOrder: orden DELIVERED no se puede eliminar")
    void deleteOrder_delivered_forbidden() {
        Long orderId = 1L;

        OrderEntity deliveredOrder = new OrderEntity();
        deliveredOrder.setId(orderId);
        deliveredOrder.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(deliveredOrder));

        assertThrows(IllegalOperationException.class,
                () -> orderService.deleteOrder(orderId));

        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("updateOrder: orden no encontrada lanza EntityNotFoundException")
    void updateOrder_notFound_throwsException() {
        Long orderId = 1L;
        OrderEntity updateData = new OrderEntity();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> orderService.updateOrder(orderId, updateData));

        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("findById: retorna orden existente correctamente")
    void findById_ok() throws Exception {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        OrderEntity result = orderService.findById(100L);
        assertNotNull(result);
        assertEquals(order, result);
        verify(orderRepository).findById(100L);
    }

    @Test
    @DisplayName("findAll: retorna lista completa")
    void findAll_ok() {
        List<OrderEntity> mockList = List.of(order);
        when(orderRepository.findAll()).thenReturn(mockList);
        List<OrderEntity> result = orderService.findAll();
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("getAllOrders y getOrderById delegan correctamente")
    void getAllOrders_getOrderById_ok() throws Exception {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        List<OrderEntity> all = orderService.getAllOrders();
        OrderEntity byId = orderService.getOrderById(100L);

        assertEquals(1, all.size());
        assertEquals(order, byId);
        verify(orderRepository).findAll();
        verify(orderRepository).findById(100L);
    }

    @Test
    @DisplayName("isValidTransition: valida transiciones permitidas y prohibidas")
    void isValidTransition_cobertura() {
        assertTrue(invokeIsValid(OrderStatus.PENDING, OrderStatus.CONFIRMED));
        assertFalse(invokeIsValid(OrderStatus.PAID, OrderStatus.CONFIRMED));
        assertFalse(invokeIsValid(OrderStatus.CANCELLED, OrderStatus.PAID));
    }

    // Helper para probar método privado mediante reflexión
    private boolean invokeIsValid(OrderStatus current, OrderStatus target) {
        try {
            var m = OrderService.class.getDeclaredMethod("isValidTransition", OrderStatus.class, OrderStatus.class);
            m.setAccessible(true);
            return (boolean) m.invoke(orderService, current, target);
        } catch (Exception e) {
            fail(e);
            return false;
        }
    }

    @Test
    @DisplayName("calcLineSubtotal retorna subtotal correctamente")
    void calcLineSubtotal_ok() {
        detail.setSubtotal(75.0);
        Double subtotal = orderService.calcLineSubtotal(detail);
        assertEquals(75.0, subtotal);
    }

    @Test
    @DisplayName("recalculateTotalAmount: calcula total con descuento")
    void recalculateTotalAmount_ok() throws Exception {
        detail.setSubtotal(100.0);
        order.setDiscount(0.1); // 10%
        order.setOrderDetails(List.of(detail));

        var m = OrderService.class.getDeclaredMethod("recalculateTotalAmount", OrderEntity.class);
        m.setAccessible(true);
        double total = (double) m.invoke(orderService, order);

        assertEquals(90.0, total, 0.001);
    }

    @Test
    @DisplayName("attachProductIfOnlyId: lanza excepción si producto nulo")
    void attachProductIfOnlyId_productNull() throws Exception {
        OrderDetailEntity d = new OrderDetailEntity();

        var m = OrderService.class.getDeclaredMethod("attachProductIfOnlyId", OrderDetailEntity.class);
        m.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> m.invoke(orderService, d));
        assertTrue(ex.getCause() instanceof EntityNotFoundException);
    }

    @Test
    @DisplayName("attachProductIfOnlyId: busca producto si solo viene el id")
    void attachProductIfOnlyId_onlyId_ok() throws Exception {
        OrderDetailEntity d = new OrderDetailEntity();
        ProductEntity p = new ProductEntity();
        p.setId(5L);
        d.setProduct(p);

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        var m = OrderService.class.getDeclaredMethod("attachProductIfOnlyId", OrderDetailEntity.class);
        m.setAccessible(true);
        m.invoke(orderService, d);

        assertEquals(product, d.getProduct());
        verify(productRepository).findById(5L);
    }

    @Test
    @DisplayName("createOrder: usuario no existe -> EntityNotFoundException")
    void createOrder_userNotFound() {
        OrderEntity newOrder = new OrderEntity();

        UserEntity user = new UserEntity();
        user.setId(9L);
        newOrder.setUser(user);

        when(personRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.createOrder(newOrder));
    }

    @Test
    @DisplayName("createOrder: se guarda, status PENDING, descuenta stock")
    void createOrder_ok() throws Exception {
        detail.setSubtotal(75.0);
        order.setUser(null);
        order.setDiscount(0.0);

        // Mock product availability
        // product stock is 10, req is 3. OK.

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity result = orderService.createOrder(order);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(7, product.getStock()); // 10 - 3

        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("createOrder: stock insuficiente -> IllegalOperationException")
    void createOrder_notEnoughStock() {
        product.setStock(2); // Requesting 3
        order.setUser(null);

        assertThrows(IllegalOperationException.class, () -> orderService.createOrder(order));

        verify(productRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder: cancelada devuelve stock")
    void changeStatus_cancelled_returnsStock() throws Exception {
        Long orderId = 100L;

        // Setup: Order created and PENDING. Stock was nominally already deducted in
        // real life,
        // but here our 'product' object is fresh from setUp with stock=10.
        // Let's assume stock WAS 10 BEFORE order, but now it is 7 because order is
        // pending.
        product.setStock(7);

        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(orderId, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, updated.getStatus());
        assertEquals(10, product.getStock()); // 7 + 3
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("changeStatus -> DELIVERED: permitido desde SHIPPED")
    void changeStatus_delivered_ok() throws Exception {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderEntity updated = orderService.changeStatus(order.getId(), OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, updated.getStatus());
        verify(orderRepository).save(order);
    }

}