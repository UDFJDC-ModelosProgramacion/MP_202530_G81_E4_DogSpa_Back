package co.edu.udistrital.mdp.back.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.entities.OrderEntity;
import co.edu.udistrital.mdp.back.entities.PersonEntity;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.OrderRepository;
import co.edu.udistrital.mdp.back.repositories.PersonRepository;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import jakarta.transaction.Transactional;

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

    // Cambia el estado de la orden y aplica reglas de negocio
    @Transactional
    public OrderEntity changeStatus(Long orderId, String newStatus) throws IllegalOperationException, EntityNotFoundException {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if ("PAID".equals(order.getStatus())) {
            throw new IllegalOperationException("Cannot change status of a PAID order");
        }
        if ("CANCELLED".equals(order.getStatus())) {
            throw new IllegalOperationException("Cannot change status of a CANCELLED order");
        }

        switch (newStatus) {
            case "CONFIRMED":
                confirmOrder(order);
                break;
            case "PAID":
                payOrder(order);
                break;
            case "CANCELLED":
                cancelOrder(order);
                break;
            default:
                order.setStatus(newStatus);
        }
        return orderRepository.save(order);
    }

    // CONFIRMED: verificar stock y reservar
    private void confirmOrder(OrderEntity order) throws IllegalOperationException {
        for (OrderDetailEntity detail : order.getOrderDetails()) {
            ProductEntity product = detail.getProduct();
            int reserved = orderDetailRepository.countReservedForProduct(product.getId());
            int available = product.getStock() - reserved;
            if (available < detail.getQuantity()) {
                throw new IllegalOperationException("Not enough stock for product: " + product.getName());
            }
        }
        order.setStatus("CONFIRMED");
    }

    // PAID: descontar stock reservado
    private void payOrder(OrderEntity order) {
        for (OrderDetailEntity detail : order.getOrderDetails()) {
            ProductEntity product = detail.getProduct();
            product.setStock(product.getStock() - detail.getQuantity());
            productRepository.save(product);
        }
        order.setStatus("PAID");
    }

    // CANCELLED: solo cambia el estado
    private void cancelOrder(OrderEntity order) {
        order.setStatus("CANCELLED");
    }


    // No se permite editar cliente, moneda, ítems o precios si está PAID o CANCELLED
    @Transactional
    public OrderEntity updateOrder(Long orderId, OrderEntity updatedOrder) throws IllegalOperationException, EntityNotFoundException {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if ("PAID".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new IllegalOperationException("Cannot edit client, currency, items or prices when order is PAID or CANCELLED");
        }
        // Aquí se pueden actualizar solo los campos permitidos
        // order.set... (otros campos editables)
        return orderRepository.save(order);
    }

    // No se puede eliminar una orden PAID; solo anulación vía CANCELLED
    @Transactional
    public void deleteOrder(Long orderId) throws EntityNotFoundException, IllegalOperationException {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        if ("PAID".equals(order.getStatus())) {
            throw new IllegalOperationException("Cannot delete a PAID order. Use CANCELLED status instead.");
        }
        orderRepository.delete(order);
    }
}
