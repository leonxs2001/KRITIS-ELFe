package de.thb.kritis_elfe.enums;

public enum ValueChangedType {
    UP("up"), DOWN("down"), EQUAL("equal"), UNEQUAL("unequal");

    private String representation;

    ValueChangedType(String representation){
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }
}
