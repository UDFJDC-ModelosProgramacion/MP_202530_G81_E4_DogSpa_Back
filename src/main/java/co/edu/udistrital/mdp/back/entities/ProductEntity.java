package co.edu.udistrital.mdp.back.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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
    private Double price;
    private Integer stock;

    @PodamExclude
    @OneToMany(mappedBy = "product")
    private List<MultimediaEntity> multimedia = new ArrayList<>();

    @PodamExclude
    @ManyToMany
    private List<ShoppingCartEntity> shoppingCarts = new ArrayList<>();

    @PodamExclude
    @OneToMany(mappedBy = "product")
    private List<OrderDetailEntity> orderDetails = new ArrayList<>();
}
