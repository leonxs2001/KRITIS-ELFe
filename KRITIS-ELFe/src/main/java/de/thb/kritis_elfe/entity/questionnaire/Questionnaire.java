package de.thb.kritis_elfe.entity.questionnaire;

import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.Ressort;
import de.thb.kritis_elfe.entity.Report;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Entity
@Table
public class Questionnaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime date;

    @ManyToOne
    private Report report;

    @ManyToOne
    private FederalState federalState;

    @ManyToOne
    private Ressort ressort;

    @OneToMany(mappedBy = "questionnaire")
    @OrderBy("branch.id ASC")
    private List<BranchQuestionnaire> branchQuestionnaires;

    public String getDateAsString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm");

        String formattedDate = date.format(formatter);
        String formattedTime = date.format(formatter2);
        return "am " + formattedDate + " um " + formattedTime + " Uhr";
    }

    //EQUALS & HASHCODE
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Questionnaire that = (Questionnaire) o;
        return false;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
