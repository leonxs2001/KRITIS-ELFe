package de.thb.webbaki.entity.questionnaire;

import de.thb.webbaki.entity.FederalState;
import de.thb.webbaki.entity.Snapshot;
import de.thb.webbaki.entity.User;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
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
    private Snapshot snapshot;

    @ManyToOne
    private FederalState federalState;

    @OneToMany(mappedBy = "questionnaire")
    @OrderBy("branch.id ASC")
    private List<BranchQuestionnaire> branchQuestionnaires;

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
