package co.edu.udistrital.mdp.back.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class ProductEntity extends BaseEntity {
    private String name;
    private String category;
    private String description;
    private Integer popularity;
    private Double price;

    @PodamExclude
    @ManyToMany
    private List<ShoppingCartEntity> shoppingCarts = new ArrayList<>();
}
