package co.edu.udistrital.mdp.back.controllers;
import java.lang.reflect.Type;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.udistrital.mdp.back.dto.OrderDetailDTO;
import co.edu.udistrital.mdp.back.entities.OrderDetailEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.services.OrderDetailService;


@RestController
@RequestMapping("/api/order-details")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;
    private final ModelMapper modelMapper;

    public OrderDetailController(OrderDetailService orderDetailService, ModelMapper modelMapper) {
        this.orderDetailService = orderDetailService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<OrderDetailDTO> findAll() {
        List<OrderDetailEntity> entities = orderDetailService.getAllOrderDetails();
        Type listType = new TypeToken<List<OrderDetailDTO>>() {}.getType();
        return modelMapper.map(entities, listType);
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public OrderDetailDTO findOne(@PathVariable Long id) throws EntityNotFoundException {
        OrderDetailEntity entity = orderDetailService.getOrderDetailById(id);
        return modelMapper.map(entity, OrderDetailDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public OrderDetailDTO create(@RequestBody OrderDetailDTO dto) {
        OrderDetailEntity entity = modelMapper.map(dto, OrderDetailEntity.class);
        OrderDetailEntity created = orderDetailService.addOrderDetail(dto.getId(), entity);
        return modelMapper.map(created, OrderDetailDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public OrderDetailDTO update(@PathVariable Long id, @RequestBody OrderDetailDTO dto)
        throws EntityNotFoundException {
        OrderDetailEntity entity = modelMapper.map(dto, OrderDetailEntity.class);
        OrderDetailEntity updated = orderDetailService.updateOrderDetail(id, entity);
        return modelMapper.map(updated, OrderDetailDTO.class);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws EntityNotFoundException {
        orderDetailService.deleteOrderDetail(id);
    }
}

