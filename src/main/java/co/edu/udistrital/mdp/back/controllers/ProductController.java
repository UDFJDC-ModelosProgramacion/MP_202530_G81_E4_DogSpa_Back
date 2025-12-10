package co.edu.udistrital.mdp.back.controllers;

import java.lang.reflect.Type;

import co.edu.udistrital.mdp.back.dto.ProductDTO;
import co.edu.udistrital.mdp.back.dto.ProductDetailDTO;
import co.edu.udistrital.mdp.back.entities.ProductEntity;
import co.edu.udistrital.mdp.back.services.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ModelMapper modelMapper;

    public ProductController(ProductService productService, ModelMapper modelMapper) {
        this.productService = productService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductDetailDTO> listAll() {                      
        List<ProductEntity> products = productService.findAll();
        Type listType = new TypeToken<List<ProductDetailDTO>>() {}.getType(); 
        return modelMapper.map(products, listType);
    }

    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductDetailDTO getOne(@PathVariable Long productId) { 
        ProductEntity product = productService.getByIdOrThrow(productId);
        return modelMapper.map(product, ProductDetailDTO.class);   
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO create(@RequestBody ProductDTO body) {
        ProductEntity product = modelMapper.map(body, ProductEntity.class);
        ProductEntity created = productService.save(product);
        return modelMapper.map(created, ProductDTO.class);
    }

    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductDTO update(@PathVariable Long productId, @RequestBody ProductDTO body) {
        ProductEntity product = modelMapper.map(body, ProductEntity.class);
        ProductEntity updated = productService.update(productId, product);
        return modelMapper.map(updated, ProductDTO.class);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long productId) {
        productService.deleteById(productId);
    }

}
