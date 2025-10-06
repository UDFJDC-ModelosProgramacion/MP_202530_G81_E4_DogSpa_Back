package co.edu.udistrital.mdp.back.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udistrital.mdp.back.entities.PaymentEntity;
import co.edu.udistrital.mdp.back.entities.ShoppingCartEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.PaymentRepository;
import co.edu.udistrital.mdp.back.repositories.ShoppingCartRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for Payment business logic
 * 
 * @author Alexander Morales Ujueta
 */
@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    private static final List<String> VALID_PAYMENT_METHODS = Arrays.asList(
            "cash", "credit_card", "debit_card", "transfer", "pse", "nequi", "daviplata", "paypal");

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_PROCESSING = "processing";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_FAILED = "failed";
    private static final String STATUS_CANCELLED = "cancelled";

    /**
     * Returns all payments
     * 
     * @return List of all payments
     */
    @Transactional
    public List<PaymentEntity> getPayments() {
        log.info("Starting process to query all payments");
        return paymentRepository.findAll();
    }

    /**
     * Finds a payment by ID
     * 
     * @param paymentId The payment identifier
     * @return The payment found
     * @throws EntityNotFoundException If payment does not exist
     */
    @Transactional
    public PaymentEntity getPayment(Long paymentId) throws EntityNotFoundException {
        log.info("Starting process to query payment with id = {0}", paymentId);
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with id = " + paymentId + " not found"));
        log.info("Finishing process to query payment with id = {0}", paymentId);
        return paymentEntity;
    }

    /**
     * Creates a new payment
     * 
     * Business Rule: Cannot create a payment without a valid payment method
     * 
     * @param paymentEntity The payment to create
     * @return The created payment
     * @throws IllegalOperationException If there is no valid payment method
     * @throws EntityNotFoundException If associated shopping cart does not exist
     */
    @Transactional
    public PaymentEntity createPayment(PaymentEntity paymentEntity)
            throws IllegalOperationException, EntityNotFoundException {
        log.info("Starting process to create payment");

        if (paymentEntity.getMethod() == null || paymentEntity.getMethod().trim().isEmpty()) {
            throw new IllegalOperationException("Cannot create a payment without a payment method");
        }

        String method = paymentEntity.getMethod().toLowerCase().trim();
        if (!VALID_PAYMENT_METHODS.contains(method)) {
            throw new IllegalOperationException(
                    "Invalid payment method: " + paymentEntity.getMethod() + 
                    ". Valid methods: " + String.join(", ", VALID_PAYMENT_METHODS));
        }

        if (paymentEntity.getShoppingCart() == null || paymentEntity.getShoppingCart().getId() == null) {
            throw new IllegalOperationException("Cannot create a payment without an associated shopping cart");
        }

        ShoppingCartEntity shoppingCart = shoppingCartRepository.findById(paymentEntity.getShoppingCart().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shopping cart with id = " + paymentEntity.getShoppingCart().getId() + " not found"));

        if (paymentEntity.getAmount() <= 0) {
            throw new IllegalOperationException("Payment amount must be greater than zero");
        }

        paymentEntity.setShoppingCart(shoppingCart);
        paymentEntity.setMethod(method);
        
        if (paymentEntity.getDate() == null) {
            paymentEntity.setDate(LocalDateTime.now());
        }
        
        if (paymentEntity.getStatus() == null || paymentEntity.getStatus().trim().isEmpty()) {
            paymentEntity.setStatus(STATUS_PENDING);
        }

        log.info("Finishing process to create payment");
        return paymentRepository.save(paymentEntity);
    }

    /**
     * Updates a payment
     * 
     * Business Rule: Cannot update payment if it has already been processed
     * 
     * @param paymentId The payment identifier
     * @param payment The payment with updated data
     * @return The updated payment
     * @throws EntityNotFoundException If payment does not exist
     * @throws IllegalOperationException If payment has already been processed
     */
    @Transactional
    public PaymentEntity updatePayment(Long paymentId, PaymentEntity payment)
            throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to update payment with id = {0}", paymentId);

        PaymentEntity existingPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with id = " + paymentId + " not found"));

        if (STATUS_COMPLETED.equalsIgnoreCase(existingPayment.getStatus())) {
            throw new IllegalOperationException("Cannot update a payment that has already been completed");
        }

        if (STATUS_FAILED.equalsIgnoreCase(existingPayment.getStatus())) {
            throw new IllegalOperationException("Cannot update a payment that has failed");
        }

        if (payment.getMethod() != null && !payment.getMethod().trim().isEmpty()) {
            String method = payment.getMethod().toLowerCase().trim();
            if (!VALID_PAYMENT_METHODS.contains(method)) {
                throw new IllegalOperationException(
                        "Invalid payment method: " + payment.getMethod() + 
                        ". Valid methods: " + String.join(", ", VALID_PAYMENT_METHODS));
            }
            existingPayment.setMethod(method);
        }

        if (payment.getAmount() > 0) {
            existingPayment.setAmount(payment.getAmount());
        }

        if (payment.getStatus() != null && !payment.getStatus().trim().isEmpty()) {
            existingPayment.setStatus(payment.getStatus());
        }

        log.info("Finishing process to update payment with id = {0}", paymentId);
        return paymentRepository.save(existingPayment);
    }

    /**
     * Deletes a payment
     * 
     * Business Rule: Cannot delete a payment associated with a completed order
     * 
     * @param paymentId The payment identifier
     * @throws EntityNotFoundException If payment does not exist
     * @throws IllegalOperationException If payment is associated with a completed order
     */
    @Transactional
    public void deletePayment(Long paymentId) throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to delete payment with id = {0}", paymentId);

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with id = " + paymentId + " not found"));

        if (STATUS_COMPLETED.equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalOperationException("Cannot delete a payment associated with a completed order");
        }

        paymentRepository.delete(payment);
        log.info("Finishing process to delete payment with id = {0}", paymentId);
    }

    /**
     * Processes a payment (changes status to processing)
     * 
     * @param paymentId The payment identifier
     * @return The updated payment
     * @throws EntityNotFoundException If payment does not exist
     * @throws IllegalOperationException If payment cannot be processed
     */
    @Transactional
    public PaymentEntity processPayment(Long paymentId) throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to process payment with id = {0}", paymentId);

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with id = " + paymentId + " not found"));

        if (!STATUS_PENDING.equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalOperationException(
                    "Only pending payments can be processed. Current status: " + payment.getStatus());
        }

        payment.setStatus(STATUS_PROCESSING);
        log.info("Finishing process to process payment with id = {0}", paymentId);
        return paymentRepository.save(payment);
    }

    /**
     * Completes a payment (changes status to completed)
     * 
     * @param paymentId The payment identifier
     * @return The updated payment
     * @throws EntityNotFoundException If payment does not exist
     * @throws IllegalOperationException If payment cannot be completed
     */
    @Transactional
    public PaymentEntity completePayment(Long paymentId) throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to complete payment with id = {0}", paymentId);

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with id = " + paymentId + " not found"));

        if (!STATUS_PROCESSING.equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalOperationException(
                    "Only processing payments can be completed. Current status: " + payment.getStatus());
        }

        payment.setStatus(STATUS_COMPLETED);
        log.info("Finishing process to complete payment with id = {0}", paymentId);
        return paymentRepository.save(payment);
    }

    /**
     * Cancels a payment (changes status to cancelled)
     * 
     * @param paymentId The payment identifier
     * @return The updated payment
     * @throws EntityNotFoundException If payment does not exist
     * @throws IllegalOperationException If payment cannot be cancelled
     */
    @Transactional
    public PaymentEntity cancelPayment(Long paymentId) throws EntityNotFoundException, IllegalOperationException {
        log.info("Starting process to cancel payment with id = {0}", paymentId);

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment with id = " + paymentId + " not found"));

        if (STATUS_COMPLETED.equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalOperationException("Cannot cancel a completed payment");
        }

        payment.setStatus(STATUS_CANCELLED);
        log.info("Finishing process to cancel payment with id = {0}", paymentId);
        return paymentRepository.save(payment);
    }

    /**
     * Gets all payments by shopping cart
     * 
     * @param shoppingCartId The shopping cart identifier
     * @return List of payments
     * @throws EntityNotFoundException If shopping cart does not exist
     */
    @Transactional
    public List<PaymentEntity> getPaymentsByShoppingCart(Long shoppingCartId) throws EntityNotFoundException {
        log.info("Starting process to query payments for shopping cart with id = {0}", shoppingCartId);

        ShoppingCartEntity shoppingCart = shoppingCartRepository.findById(shoppingCartId)
                .orElseThrow(() -> new EntityNotFoundException("Shopping cart with id = " + shoppingCartId + " not found"));

        log.info("Finishing process to query payments for shopping cart with id = {0}", shoppingCartId);
        return shoppingCart.getPayments();
    }
}