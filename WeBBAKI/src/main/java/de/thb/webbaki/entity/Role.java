package de.thb.webbaki.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String representation;

    @ManyToMany(mappedBy = "roles")
    private Collection<User> users;

    public Role(String name, String representation) {
        this.name = name;
        this.representation = representation;
    }

    @Override
    public String toString() {
        return representation;
    }
}