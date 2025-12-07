package co.edu.udistrital.mdp.back.controllers;

import java.lang.reflect.Type;

import co.edu.udistrital.mdp.back.entities.OrderEntity;
import co.edu.udistrital.mdp.back.entities.OrderStatus;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import co.edu.udistrital.mdp.back.dto.OrderDTO;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final ModelMapper modelMapper;

    public OrderController(OrderService orderService, ModelMapper modelMapper) {
        this.orderService = orderService;
        this.modelMapper = modelMapper;
    }
    /* ========================= CRUD ========================= */

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderDTO> listAll() {
        List<OrderEntity> orders = orderService.getAllOrders();
        Type listType = new TypeToken<List<OrderDTO>>() {}.getType();
        return modelMapper.map(orders, listType);
    }

    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public OrderDTO findOne(@PathVariable Long orderId) throws EntityNotFoundException {
        OrderEntity order = orderService.getOrderById(orderId);
        return modelMapper.map(order, OrderDTO.class);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDTO create(@RequestBody OrderEntity body) throws EntityNotFoundException {
        OrderEntity created = orderService.createOrder(body);
        return modelMapper.map(created, OrderDTO.class);
    }

    @PutMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public OrderDTO update(@PathVariable Long orderId, @RequestBody OrderEntity body)
            throws EntityNotFoundException, IllegalOperationException {
        OrderEntity updated = orderService.updateOrder(orderId, body);
        return modelMapper.map(updated, OrderDTO.class);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long orderId)
            throws EntityNotFoundException, IllegalOperationException {
        orderService.deleteOrder(orderId);
    }

    /* ========================= ESTADO ========================= */

    @PatchMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.OK)
    public OrderEntity changeStatus(
            @PathVariable Long orderId,
            @RequestParam("to") OrderStatus newStatus
    ) throws EntityNotFoundException, IllegalOperationException {
        return orderService.changeStatus(orderId, newStatus);
    }
}
