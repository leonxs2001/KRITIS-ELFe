package de.thb.kritis_elfe.enums;

public enum ScenarioType {
    TEXT("Reine Textform", false), AUSWAHL("Auswahl mit Kommentar", true);
    private String description;
    private boolean withSelection;

    ScenarioType(String description, boolean withSelection){
        this.description = description;
        this.withSelection = withSelection;
    }

    public boolean isWithSelection() {
        return withSelection;
    }
}
