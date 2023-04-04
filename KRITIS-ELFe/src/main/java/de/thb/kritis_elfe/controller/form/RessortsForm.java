package de.thb.kritis_elfe.controller.form;

import de.thb.kritis_elfe.entity.Ressort;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RessortsForm {
    private List<Ressort> ressorts;
}
