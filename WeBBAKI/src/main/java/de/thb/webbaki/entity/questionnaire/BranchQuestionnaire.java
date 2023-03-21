package de.thb.webbaki.entity.questionnaire;

import de.thb.webbaki.entity.Branch;
import de.thb.webbaki.entity.Snapshot;
import de.thb.webbaki.entity.User;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
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
public class BranchQuestionnaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @OneToMany(mappedBy = "branchQuestionnaire")
    @OrderBy("scenario ASC")
    private List<UserScenario> userScenarios;

    //EQUALS & HASHCODE
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        BranchQuestionnaire that = (BranchQuestionnaire) o;
        return false;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
