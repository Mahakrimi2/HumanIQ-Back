package pfe.HumanIQ.HumanIQ.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor

public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Génération automatique de l'ID
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, length = 50)
    private UserRole name;

    public Role(UserRole roleName) {
        this.name=roleName;
    }

    public Role() {
    }

    public Long id() {
        return id;
    }

    public Role setId(Long id) {
        this.id = id;
        return this;
    }

    public UserRole  getName() {
        return name;
    }

    public Role setName(UserRole name) {
        this.name = name;
        return this;
    }


}
