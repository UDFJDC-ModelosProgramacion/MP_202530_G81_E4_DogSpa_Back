package co.edu.udistrital.mdp.back.services;

import java.util.List;

import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.entities.OrderEntity;
import co.edu.udistrital.mdp.back.entities.OrderStatus;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.OrderDetailRepository;
import co.edu.udistrital.mdp.back.repositories.OrderRepository;
import co.edu.udistrital.mdp.back.repositories.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository; 

    private final String orderDetailNotFoundMessage = "Order detail not found";

    private void validateQuantity(int quantity) throws IllegalOperationException {
        if (quantity <= 0) {
            throw new IllegalOperationException("Quantity must be greater than 0");
        }
    }

    private double computeLineSubtotal(OrderDetailEntity d) throws IllegalOperationException {
        ProductEntity p = d.getProduct();
        if (p == null || p.getId() == null) {
            throw new IllegalOperationException("Order detail must reference a valid product");
        }
        ProductEntity dbProduct = productRepository.findById(p.getId())
                .orElseThrow(() -> new IllegalOperationException("Product not found"));
        if (dbProduct.getPrice() == null) {
            throw new IllegalOperationException("Product has no price configured");
        }
        return dbProduct.getPrice() * d.getQuantity();
    }

    @Transactional(readOnly = true)
    public List<OrderDetailEntity> getAllOrderDetails() {
        return orderDetailRepository.findAll();
    }

    @Transactional(readOnly = true)
    public OrderDetailEntity getOrderDetailById(Long id) throws EntityNotFoundException {
        return orderDetailRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(orderDetailNotFoundMessage));
    }

   @Transactional
    public OrderDetailEntity addOrderDetail(Long orderId, OrderDetailEntity incoming)
        throws EntityNotFoundException, IllegalOperationException {

        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalOperationException("Cannot add items unless order is PENDING (current: " + order.getStatus() + ")");
        }

        validateQuantity(incoming.getQuantity());

        incoming.setOrder(order);
        incoming.setSubtotal(computeLineSubtotal(incoming));   
        OrderDetailEntity saved = orderDetailRepository.save(incoming);

        recalcAndPersistOrderTotal(order);
        return saved;
    }

    @Transactional
    public OrderDetailEntity updateOrderDetail(Long detailId, OrderDetailEntity incoming)
        throws EntityNotFoundException, IllegalOperationException {

        OrderDetailEntity existing = orderDetailRepository.findById(detailId)
            .orElseThrow(() -> new EntityNotFoundException(orderDetailNotFoundMessage));

        OrderEntity order = existing.getOrder();
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalOperationException("Cannot update items unless order is PENDING (current: " + order.getStatus() + ")");
        }

        if (incoming.getProduct() != null && incoming.getProduct().getId() != null) {
            existing.setProduct(incoming.getProduct());
        }

        validateQuantity(incoming.getQuantity());
        existing.setQuantity(incoming.getQuantity());

        existing.setSubtotal(computeLineSubtotal(existing));   
        OrderDetailEntity saved = orderDetailRepository.save(existing);

        recalcAndPersistOrderTotal(order);
        return saved;
    }

    @Transactional
    public void deleteOrderDetail(Long detailId)
            throws EntityNotFoundException, IllegalOperationException {

        OrderDetailEntity detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new EntityNotFoundException(orderDetailNotFoundMessage));

        OrderEntity order = detail.getOrder();

        if (order.getStatus() != OrderStatus.PENDING) {
            String msg = OrderStatus.isImmutable(order.getStatus())
                    ? "Cannot delete items when order is " + order.getStatus()
                    : "Cannot delete items unless order is PENDING (current: " + order.getStatus() + ")";
            throw new IllegalOperationException(msg);
        }

        orderDetailRepository.delete(detail);

        recalcAndPersistOrderTotal(order);
    }

    private void recalcAndPersistOrderTotal(OrderEntity order) {
        double subtotal = 0.0;

        if (order.getOrderDetails() != null) {
            for (OrderDetailEntity detail : order.getOrderDetails()) {
                Double lineSubtotal = detail.getSubtotal();
                if (lineSubtotal != null) {
                subtotal += lineSubtotal;
                }
            }   
        }
        double discount = order.getDiscount();
        if (discount < 0 || discount > 1) {
            throw new IllegalArgumentException("Discount must be between 0 and 1 (e.g., 0.3 = 30%).");
        }

        double total = subtotal - (subtotal * discount);
        if (total < 0) total = 0.0;

        order.setTotalAmount(total);
        orderRepository.save(order);
    }
}
