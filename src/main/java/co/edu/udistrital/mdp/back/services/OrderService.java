package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.entities.OrderEntity;
import co.edu.udistrital.mdp.back.entities.OrderStatus;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;

import co.edu.udistrital.mdp.back.repositories.OrderRepository;
import co.edu.udistrital.mdp.back.repositories.PersonRepository;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PersonRepository personRepository;

    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found";

    // Constructor injection instead of field injection
    public OrderService(OrderRepository orderRepository,
            ProductRepository productRepository,
            PersonRepository personRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.personRepository = personRepository;
    }

    @Transactional
    public OrderEntity findById(Long orderId) throws EntityNotFoundException {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND_MESSAGE));
    }

    @Transactional
    public List<OrderEntity> findAll() {
        return orderRepository.findAll();
    }

    @Transactional
    public OrderEntity createOrder(OrderEntity incoming) throws EntityNotFoundException, IllegalOperationException {
        if (incoming.getUser() != null && incoming.getUser().getId() != null) {
            var userId = incoming.getUser().getId();
            personRepository.findById(userId).orElseThrow(
                    () -> new EntityNotFoundException("User not found with id " + userId));
        }

        // Status por defecto
        if (incoming.getStatus() == null) {
            incoming.setStatus(OrderStatus.PENDING);
        }

        // FIXED: Changed getOrderDetail() to getOrderDetails() (plural)
        if (incoming.getOrderDetails() != null) {
            for (OrderDetailEntity d : incoming.getOrderDetails()) {
                attachProductIfOnlyId(d);
                d.setOrder(incoming);
                if (d.getSubtotal() == null) {
                    d.setSubtotal(calcLineSubtotal(d));
                }

                // DECREMENT STOCK ON CREATE
                ProductEntity product = d.getProduct();
                if (product.getStock() < d.getQuantity()) {
                    throw new IllegalOperationException("Not enough stock for product: " + product.getName());
                }
                product.setStock(product.getStock() - d.getQuantity());
                productRepository.save(product);
            }
        }
        incoming.setTotalAmount(recalculateTotalAmount(incoming));

        return orderRepository.save(incoming);
    }

    @Transactional
    public OrderEntity changeStatus(Long orderId, OrderStatus newStatus)
            throws IllegalOperationException, EntityNotFoundException {

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND_MESSAGE));

        OrderStatus current = order.getStatus();

        if (!isValidTransition(current, newStatus)) {
            throw new IllegalOperationException("Invalid transition: " + current + " → " + newStatus);
        }

        switch (newStatus) {
            case CONFIRMED -> confirmOrder(order);
            case PAID -> payOrder(order);
            case CANCELLED -> cancelOrder(order);
            case SHIPPED -> shipOrder(order);
            case DELIVERED -> deliverOrder(order);
            default -> order.setStatus(newStatus);
        }

        return orderRepository.save(order);
    }

    private void confirmOrder(OrderEntity order) throws IllegalOperationException {
        // Stock already reserved at creation time.
        // potentially check other business rules here.
        order.setStatus(OrderStatus.CONFIRMED);
    }

    private void payOrder(OrderEntity order) throws IllegalOperationException {
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalOperationException("Order must be CONFIRMED before payment.");
        }
        logger.info("Executing payOrder for order id={}. Order details count={}", order.getId(),
                order.getOrderDetails() == null ? 0 : order.getOrderDetails().size());

        // Stock already decremented at creation.

        order.setStatus(OrderStatus.PAID);
    }

    private void cancelOrder(OrderEntity order) {
        // Return stock
        if (order.getOrderDetails() != null) {
            for (OrderDetailEntity detail : order.getOrderDetails()) {
                ProductEntity product = detail.getProduct();
                product.setStock(product.getStock() + detail.getQuantity());
                productRepository.save(product);
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
    }

    private void shipOrder(OrderEntity order) throws IllegalOperationException {
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalOperationException("Order must be PAID before shipping.");
        }
        order.setStatus(OrderStatus.SHIPPED);
    }

    private void deliverOrder(OrderEntity order) throws IllegalOperationException {
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalOperationException("Order must be SHIPPED before delivery.");
        }
        order.setStatus(OrderStatus.DELIVERED);
    }

    @Transactional
    public OrderEntity updateOrder(Long orderId, OrderEntity incoming)
            throws IllegalOperationException, EntityNotFoundException {

        OrderEntity current = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND_MESSAGE));

        if (OrderStatus.isImmutable(current.getStatus())) {
            throw new IllegalOperationException(
                    "Orders in status " + current.getStatus()
                            + " are immutable. Only status changes via changeStatus() are allowed.");
        }

        if (incoming.getOrderDate() != null)
            current.setOrderDate(incoming.getOrderDate());
        if (incoming.getUser() != null)
            current.setUser(incoming.getUser());
        current.setDiscount(incoming.getDiscount());
        current.setTotalAmount(recalculateTotalAmount(current));

        return orderRepository.save(current);
    }

    @Transactional
    public void deleteOrder(Long orderId)
            throws EntityNotFoundException, IllegalOperationException {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_NOT_FOUND_MESSAGE));

        if (OrderStatus.isImmutable(order.getStatus())) {
            throw new IllegalOperationException("Cannot delete an order in status " + order.getStatus() + ".");
        }
        orderRepository.delete(order);
    }

    // =========================================================
    // Helpers de reglas
    // =========================================================

    private boolean isValidTransition(OrderStatus current, OrderStatus target) {
        Set<OrderStatus> allowed = switch (current) {
            case PENDING -> EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED -> EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED);
            case PAID -> EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
            case SHIPPED -> EnumSet.of(OrderStatus.DELIVERED);
            case DELIVERED -> EnumSet.noneOf(OrderStatus.class);
            case CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
        return allowed.contains(target);
    }

    private double recalculateTotalAmount(OrderEntity order) {
        double sum = 0.0;
        if (order.getOrderDetails() != null) {
            for (OrderDetailEntity d : order.getOrderDetails()) {

                Double lineTotal = null;

                lineTotal = d.getSubtotal();

                if (lineTotal != null) {
                    sum += lineTotal;
                }
            }
        }
        double discount = order.getDiscount() != 0 ? order.getDiscount() : 0.0;
        double total = sum - (sum * discount);
        return total < 0 ? 0.0 : total;
    }

    private void attachProductIfOnlyId(OrderDetailEntity d) throws EntityNotFoundException {
        if (d.getProduct() == null) {
            throw new EntityNotFoundException("Product must be provided in order detail.");
        }
        if (d.getProduct().getId() != null && (d.getProduct().getName() == null)) {
            ProductEntity product = productRepository.findById(d.getProduct().getId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Product not found with id " + d.getProduct().getId()));
            d.setProduct(product);
        }
    }

    public List<OrderEntity> getAllOrders() {
        return findAll();
    }

    public OrderEntity getOrderById(Long id) throws EntityNotFoundException {
        return findById(id);
    }

    // FIXED: Removed the incorrect getOrderDetail method that was expecting
    // incoming OrderDetailEntity
    // This method should calculate line subtotal from OrderDetailEntity
    public Double calcLineSubtotal(OrderDetailEntity detail) {
        return detail.getSubtotal();
    }
}