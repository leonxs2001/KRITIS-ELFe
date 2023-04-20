package de.thb.kritis_elfe.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Ressort {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String shortcut;

    @ManyToMany()
    private List<Branch> branches;

    @OneToMany(mappedBy = "ressort")
    List<User> users;

    public boolean belongsToBranchFromList(List<Branch> branchList){
        for(Branch branch: branchList){
            if(this.branches.contains(branch)){
                return true;
            }
        }

        return false;
    }

    public Ressort(String name, String shortcut, List<Branch> branches){
        this.name = name;
        this.shortcut = shortcut;
        this.setBranches(branches);
    }

    public String toString(){
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ressort ressort = (Ressort) o;
        return id == ressort.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
