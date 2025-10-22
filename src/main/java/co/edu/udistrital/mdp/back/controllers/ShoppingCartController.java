package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.ShoppingCartDTO;
import co.edu.udistrital.mdp.back.entities.ShoppingCartEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.ShoppingCartService;

@RestController
@RequestMapping("/api/shoppingcarts")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;
    private final ModelMapper modelMapper;

    public ShoppingCartController(ShoppingCartService shoppingCartService, ModelMapper modelMapper) {
        this.shoppingCartService = shoppingCartService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<ShoppingCartDTO> findAll() {
        List<ShoppingCartEntity> carts = shoppingCartService.getShoppingCarts();
        return modelMapper.map(carts, new TypeToken<List<ShoppingCartDTO>>() {}.getType());
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ShoppingCartDTO findOne(@PathVariable("id") Long id) throws EntityNotFoundException {
        ShoppingCartEntity cart = shoppingCartService.getShoppingCart(id);
        return modelMapper.map(cart, ShoppingCartDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ShoppingCartDTO create(@RequestBody ShoppingCartDTO shoppingCartDTO)
            throws IllegalOperationException, EntityNotFoundException {
        ShoppingCartEntity cart = modelMapper.map(shoppingCartDTO, ShoppingCartEntity.class);
        ShoppingCartEntity newCart = shoppingCartService.createShoppingCart(cart);
        return modelMapper.map(newCart, ShoppingCartDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ShoppingCartDTO update(@PathVariable("id") Long id, @RequestBody ShoppingCartDTO shoppingCartDTO)
            throws EntityNotFoundException, IllegalOperationException {
        ShoppingCartEntity cart = modelMapper.map(shoppingCartDTO, ShoppingCartEntity.class);
        ShoppingCartEntity updatedCart = shoppingCartService.updateShoppingCart(id, cart);
        return modelMapper.map(updatedCart, ShoppingCartDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) throws EntityNotFoundException, IllegalOperationException {
        shoppingCartService.deleteShoppingCart(id);
    }
}