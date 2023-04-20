package de.thb.kritis_elfe.entity;

import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private LocalDateTime date;

    @OneToMany(mappedBy = "report")
    private List<Questionnaire> questionnaires;

    public String getDateAsString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");

        String formattedDate = date.format(formatter);
        String formattedTime = date.format(formatter2);
        return "am " + formattedDate + " um " + formattedTime + " Uhr";
    }

    @Override
    public String toString(){
        return name + " erstellt " + getDateAsString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return id == report.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
