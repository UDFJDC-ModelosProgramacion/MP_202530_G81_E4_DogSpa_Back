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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private PersonRepository personRepository;

    private final String orderNotFoundMessage = "Order not found";

    @Transactional
    public OrderEntity changeStatus(Long orderId, OrderStatus newStatus)
            throws IllegalOperationException, EntityNotFoundException {

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(orderNotFoundMessage));

        OrderStatus current = order.getStatus();

        if (!isValidTransition(current, newStatus)) {
            throw new IllegalOperationException("Invalid transition: " + current + " → " + newStatus);
        }

        switch (newStatus) {
            case CONFIRMED -> confirmOrder(order); 
            case PAID      -> payOrder(order);    
            case CANCELLED -> cancelOrder(order);  
            case SHIPPED   -> shipOrder(order);   
            case DELIVERED -> deliverOrder(order); 
            default        -> order.setStatus(newStatus);
        }

        return orderRepository.save(order);
    }


    private void confirmOrder(OrderEntity order) throws IllegalOperationException {
        for (OrderDetailEntity detail : order.getOrderDetails()) {
            ProductEntity product = detail.getProduct();
            int reserved = orderDetailRepository.countReservedForProduct(product.getId()); 
            int available = product.getStock() - reserved;
            if (available < detail.getQuantity()) {
                throw new IllegalOperationException("Not enough stock for product: " + product.getName());
            }
        }
        order.setStatus(OrderStatus.CONFIRMED);
    }

    private void payOrder(OrderEntity order) throws IllegalOperationException {
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalOperationException("Order must be CONFIRMED before payment.");
        }
        for (OrderDetailEntity detail : order.getOrderDetails()) {
            ProductEntity product = detail.getProduct();
            product.setStock(product.getStock() - detail.getQuantity());
            productRepository.save(product);
        }
        order.setStatus(OrderStatus.PAID);
    }

    private void cancelOrder(OrderEntity order) {
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
            .orElseThrow(() -> new EntityNotFoundException(orderNotFoundMessage));

        if (OrderStatus.isImmutable(current.getStatus())) {
            throw new IllegalOperationException(
                "Orders in status " + current.getStatus() + " are immutable. Only status changes via changeStatus() are allowed.");
        }

        if (incoming.getOrderDate() != null) current.setOrderDate(incoming.getOrderDate());
        if (incoming.getUser() != null)      current.setUser(incoming.getUser());
        current.setDiscount(incoming.getDiscount());
        current.setTotalAmount(recalculateTotalAmount(current));

        return orderRepository.save(current);
    }

    @Transactional
    public void deleteOrder(Long orderId)
        throws EntityNotFoundException, IllegalOperationException {
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException(orderNotFoundMessage));

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
            case PENDING   -> EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED -> EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED);
            case PAID      -> EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED); 
            case SHIPPED   -> EnumSet.of(OrderStatus.DELIVERED);
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
}
