package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.ProductDTO;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.ShoppingCartProductService;

@RestController
@RequestMapping("/shoppingcarts")
public class ShoppingCartProductController {

    private final ShoppingCartProductService shoppingCartProductService;
    private final ModelMapper modelMapper;

    public ShoppingCartProductController(ShoppingCartProductService shoppingCartProductService, 
                                        ModelMapper modelMapper) {
        this.shoppingCartProductService = shoppingCartProductService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{shoppingCartId}/products")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ProductDTO> getProducts(@PathVariable Long shoppingCartId) throws EntityNotFoundException {
        List<ProductEntity> products = shoppingCartProductService.getProducts(shoppingCartId);
        return modelMapper.map(products, new TypeToken<List<ProductDTO>>() {}.getType());
    }

    @GetMapping("/{shoppingCartId}/products/{productId}")
    @ResponseStatus(code = HttpStatus.OK)
    public ProductDTO getProduct(@PathVariable Long shoppingCartId, @PathVariable Long productId)
            throws EntityNotFoundException, IllegalOperationException {
        ProductEntity product = shoppingCartProductService.getProduct(shoppingCartId, productId);
        return modelMapper.map(product, ProductDTO.class);
    }

    @PostMapping("/{shoppingCartId}/products/{productId}")
    @ResponseStatus(code = HttpStatus.OK)
    public ProductDTO addProduct(@PathVariable Long shoppingCartId, @PathVariable Long productId)
            throws EntityNotFoundException, IllegalOperationException {
        ProductEntity product = shoppingCartProductService.addProduct(shoppingCartId, productId);
        return modelMapper.map(product, ProductDTO.class);
    }

    @PutMapping("/{shoppingCartId}/products")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ProductDTO> replaceProducts(@PathVariable Long shoppingCartId,
            @RequestBody List<ProductDTO> productDTOs) throws EntityNotFoundException {
        List<ProductEntity> products = modelMapper.map(productDTOs, new TypeToken<List<ProductEntity>>() {}.getType());
        List<ProductEntity> updatedProducts = shoppingCartProductService.replaceProducts(shoppingCartId, products);
        return modelMapper.map(updatedProducts, new TypeToken<List<ProductDTO>>() {}.getType());
    }

    @DeleteMapping("/{shoppingCartId}/products/{productId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeProduct(@PathVariable Long shoppingCartId, @PathVariable Long productId)
            throws EntityNotFoundException {
        shoppingCartProductService.removeProduct(shoppingCartId, productId);
    }
}