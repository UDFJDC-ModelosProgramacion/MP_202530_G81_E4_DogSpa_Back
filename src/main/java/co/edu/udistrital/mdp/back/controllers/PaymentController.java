package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.PaymentDTO;
import co.edu.udistrital.mdp.back.entities.PaymentEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final ModelMapper modelMapper;

    public PaymentController(PaymentService paymentService, ModelMapper modelMapper) {
        this.paymentService = paymentService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<PaymentDTO> findAll() {
        List<PaymentEntity> payments = paymentService.getPayments();
        return modelMapper.map(payments, new TypeToken<List<PaymentDTO>>() {}.getType());
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public PaymentDTO findOne(@PathVariable("id") Long id) throws EntityNotFoundException {
        PaymentEntity payment = paymentService.getPayment(id);
        return modelMapper.map(payment, PaymentDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public PaymentDTO create(@RequestBody PaymentDTO paymentDTO)
            throws IllegalOperationException, EntityNotFoundException {
        PaymentEntity payment = modelMapper.map(paymentDTO, PaymentEntity.class);
        PaymentEntity newPayment = paymentService.createPayment(payment);
        return modelMapper.map(newPayment, PaymentDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public PaymentDTO update(@PathVariable("id") Long id, @RequestBody PaymentDTO paymentDTO)
            throws EntityNotFoundException, IllegalOperationException {
        PaymentEntity payment = modelMapper.map(paymentDTO, PaymentEntity.class);
        PaymentEntity updatedPayment = paymentService.updatePayment(id, payment);
        return modelMapper.map(updatedPayment, PaymentDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) throws EntityNotFoundException, IllegalOperationException {
        paymentService.deletePayment(id);
    }

    @PutMapping("/{id}/process")
    @ResponseStatus(code = HttpStatus.OK)
    public PaymentDTO process(@PathVariable("id") Long id) throws EntityNotFoundException, IllegalOperationException {
        PaymentEntity payment = paymentService.processPayment(id);
        return modelMapper.map(payment, PaymentDTO.class);
    }

    @PutMapping("/{id}/complete")
    @ResponseStatus(code = HttpStatus.OK)
    public PaymentDTO complete(@PathVariable("id") Long id) throws EntityNotFoundException, IllegalOperationException {
        PaymentEntity payment = paymentService.completePayment(id);
        return modelMapper.map(payment, PaymentDTO.class);
    }

    @PutMapping("/{id}/cancel")
    @ResponseStatus(code = HttpStatus.OK)
    public PaymentDTO cancel(@PathVariable("id") Long id) throws EntityNotFoundException, IllegalOperationException {
        PaymentEntity payment = paymentService.cancelPayment(id);
        return modelMapper.map(payment, PaymentDTO.class);
    }
}