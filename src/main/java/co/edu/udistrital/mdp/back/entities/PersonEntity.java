package co.edu.udistrital.mdp.back.entities;
    import jakarta.persistence.Entity;
    import lombok.Data;
    @Data
    @Entity
public class PersonEntity extends BaseEntity{
    protected String name;
    protected String lastname;
    protected String email;
    protected String telephone;
    protected String address;
    protected String password;
}
