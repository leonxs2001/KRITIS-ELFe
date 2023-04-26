package de.thb.kritis_elfe.service.helper.report;

import de.thb.kritis_elfe.enums.ValueChangedType;

public class ReportValue {
    private short value;
    private ValueChangedType valueChangedType;

    public ReportValue(short value, ValueChangedType valueChangedType) {
        this.value = value;
        this.valueChangedType = valueChangedType;
    }

    public ReportValue(short value) {
        this(value, ValueChangedType.EQUAL);
    }
    public ReportValue() {
        this((short)0);
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    public ValueChangedType getValueChangedType() {
        return valueChangedType;
    }

    public void setValueChangedType(ValueChangedType valueChangedType) {
        this.valueChangedType = valueChangedType;
    }

    public String getValueColorAsHTMLString(){
        switch (value){

            case 1:
                return "rgb(102, 255, 102)";
            case 2:
                return "rgb(255, 255, 102)";
            case 3:
                return "rgb(255, 178, 102)";
            case 4:
                return "rgb(255, 102, 102)";
            default:
                return "white";
        }
    }

    public String getValueColorAsWordString(){
        switch (value){

            case 1:
                return "66FF66";
            case 2:
                return "FFFF66";
            case 3:
                return "FFB266";
            case 4:
                return "FF6666";
            default:
                return "FFFFFF";
        }
    }
}
