package de.thb.kritis_elfe.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity(name = "branch")
@NoArgsConstructor
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @ManyToMany(mappedBy = "branches")
    private List<Ressort> ressorts;

    @ManyToOne
    @JoinColumn(name="sector_id", nullable=false)
    private Sector sector;

    @Override
    public String toString() {
        return name;
    }

    public Branch(String name, Sector sector){
        this.name = name;
        this.sector = sector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return id == branch.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
