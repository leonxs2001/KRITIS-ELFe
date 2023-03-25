package de.thb.kritis_elfe.entity;

import de.thb.kritis_elfe.enums.ScenarioType;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name="scenario")
@Table
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    boolean active;
    short positionInRow;

    @Enumerated(EnumType.ORDINAL)
    ScenarioType scenarioType;

    @Column(length = 1024)
    @Size(max = 1024)
    private String description;

    public Scenario(String description, ScenarioType scenarioType, short positionInRow){
        active = true;
        this.positionInRow = positionInRow;
        this.description = description;
        this.scenarioType = scenarioType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scenario scenario = (Scenario) o;
        return id == scenario.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}