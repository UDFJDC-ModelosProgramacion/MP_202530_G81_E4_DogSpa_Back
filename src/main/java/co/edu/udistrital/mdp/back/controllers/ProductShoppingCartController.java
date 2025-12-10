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
import co.edu.udistrital.mdp.back.services.ProductShoppingCartService;

@RestController
@RequestMapping("/products")
public class ProductShoppingCartController {

    private final ProductShoppingCartService productShoppingCartService;
    private final ModelMapper modelMapper;

    public ProductShoppingCartController(ProductShoppingCartService productShoppingCartService, 
                                        ModelMapper modelMapper) {
        this.productShoppingCartService = productShoppingCartService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{productId}/shoppingcarts")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ShoppingCartDTO> getShoppingCarts(@PathVariable Long productId) throws EntityNotFoundException {
        List<ShoppingCartEntity> carts = productShoppingCartService.getShoppingCarts(productId);
        return modelMapper.map(carts, new TypeToken<List<ShoppingCartDTO>>() {}.getType());
    }

    @GetMapping("/{productId}/shoppingcarts/{shoppingCartId}")
    @ResponseStatus(code = HttpStatus.OK)
    public ShoppingCartDTO getShoppingCart(@PathVariable Long productId, @PathVariable Long shoppingCartId)
            throws EntityNotFoundException, IllegalOperationException {
        ShoppingCartEntity cart = productShoppingCartService.getShoppingCart(productId, shoppingCartId);
        return modelMapper.map(cart, ShoppingCartDTO.class);
    }

    @PostMapping("/{productId}/shoppingcarts/{shoppingCartId}")
    @ResponseStatus(code = HttpStatus.OK)
    public ShoppingCartDTO addShoppingCart(@PathVariable Long productId, @PathVariable Long shoppingCartId)
            throws EntityNotFoundException {
        ShoppingCartEntity cart = productShoppingCartService.addShoppingCart(productId, shoppingCartId);
        return modelMapper.map(cart, ShoppingCartDTO.class);
    }

    @PutMapping("/{productId}/shoppingcarts")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ShoppingCartDTO> replaceShoppingCarts(@PathVariable Long productId,
            @RequestBody List<ShoppingCartDTO> shoppingCartDTOs) throws EntityNotFoundException {
        List<ShoppingCartEntity> carts = modelMapper.map(shoppingCartDTOs,
                new TypeToken<List<ShoppingCartEntity>>() {}.getType());
        List<ShoppingCartEntity> updatedCarts = productShoppingCartService.replaceShoppingCarts(productId, carts);
        return modelMapper.map(updatedCarts, new TypeToken<List<ShoppingCartDTO>>() {}.getType());
    }

    @DeleteMapping("/{productId}/shoppingcarts/{shoppingCartId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeShoppingCart(@PathVariable Long productId, @PathVariable Long shoppingCartId)
            throws EntityNotFoundException {
        productShoppingCartService.removeShoppingCart(productId, shoppingCartId);
    }
}