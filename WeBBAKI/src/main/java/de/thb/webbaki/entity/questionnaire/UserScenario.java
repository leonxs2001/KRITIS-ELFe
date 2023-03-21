package de.thb.webbaki.entity.questionnaire;

import de.thb.webbaki.entity.Branch;
import de.thb.webbaki.entity.Scenario;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.text.DecimalFormat;

/**
 * A UserScenario is one filled row of  a Questionnaire.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class UserScenario{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private short value;

    @Column(length = 1000)
    @Size(max = 10000)
    private String comment;

    @ManyToOne
    private Scenario scenario;

    @ManyToOne
    @JoinColumn(name = "branchQuestionnaire_id", nullable = false)
    private BranchQuestionnaire branchQuestionnaire;

}
