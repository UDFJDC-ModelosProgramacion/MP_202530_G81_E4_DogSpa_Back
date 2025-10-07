package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.entities.OrderEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Regla: quantity debe ser entero > 0
    private void validateQuantity(int quantity) throws IllegalOperationException {
        if (quantity <= 0) {
            throw new IllegalOperationException("Quantity must be greater than 0");
        }
    }

    // Agregar un nuevo detalle a una orden
    @Transactional
    public OrderDetailEntity addOrderDetail(Long orderId, OrderDetailEntity detail) throws EntityNotFoundException, IllegalOperationException {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if ("PAID".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new IllegalOperationException("Cannot add items to a PAID or CANCELLED order");
        }

        validateQuantity(detail.getQuantity());
        detail.setOrder(order);
        return orderDetailRepository.save(detail);
    }

    // Actualizar un detalle de orden (no se puede cambiar price si la orden está CONFIRMED o superior)
    @Transactional
    public OrderDetailEntity updateOrderDetail(Long detailId, OrderDetailEntity updatedDetail) throws EntityNotFoundException, IllegalOperationException {
        OrderDetailEntity existingDetail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new EntityNotFoundException("Order detail not found"));

        OrderEntity order = existingDetail.getOrder();

        if ("PAID".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new IllegalOperationException("Cannot update items in a PAID or CANCELLED order");
        }

        validateQuantity(updatedDetail.getQuantity());

        // No se puede cambiar price si la orden está CONFIRMED o superior
        if (isStatusConfirmedOrHigher(order.getStatus()) &&
                !existingDetail.getSubtotal().equals(updatedDetail.getSubtotal())) {
            throw new IllegalOperationException("Cannot change price when order is CONFIRMED or higher");
        }

        existingDetail.setQuantity(updatedDetail.getQuantity());
        // Solo permite cambiar subtotal si la orden no está CONFIRMED o superior
        if (!isStatusConfirmedOrHigher(order.getStatus())) {
            existingDetail.setSubtotal(updatedDetail.getSubtotal());
        }

        return orderDetailRepository.save(existingDetail);
    }

    // Eliminar un detalle de orden (no se puede si la orden está PAID o CANCELLED)
    @Transactional
    public void deleteOrderDetail(Long detailId) throws EntityNotFoundException, IllegalOperationException {
        OrderDetailEntity detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new EntityNotFoundException("Order detail not found"));

        OrderEntity order = detail.getOrder();

        if ("PAID".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new IllegalOperationException("Cannot delete items from a PAID or CANCELLED order");
        }

        orderDetailRepository.delete(detail);
    }

    // Utilidad: determina si el estado es CONFIRMED o superior
    private boolean isStatusConfirmedOrHigher(String status) {
        return "CONFIRMED".equals(status) || "PAID".equals(status) || "CANCELLED".equals(status);
    }
}
