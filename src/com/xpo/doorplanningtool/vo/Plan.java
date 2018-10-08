package com.xpo.doorplanningtool.vo;

public class Plan {

    private String sic;
    private String beginning_date;
    private String ending_date;
    private String instruction_date;
    private String previous_instruction_date;
    private boolean sending_email_ind;
    private boolean fac_shift;
    private boolean is_exception_date;
    private String shift = "OTB";
    private String shift_abbr ="O";
    private String bypass_frequency = "0.8";
    private String loc_load_plan_shift_cd = "O";

    public Plan(String sic, String beginning_date, String ending_date, String instruction_date, String previous_instruction_date, boolean sending_email_ind, boolean fac_shift, boolean is_exception_date) {
        this.sic = sic;
        this.beginning_date = beginning_date;
        this.ending_date = ending_date;
        this.instruction_date = instruction_date;
        this.previous_instruction_date = previous_instruction_date;
        this.sending_email_ind = sending_email_ind;
        this.fac_shift = fac_shift;
        this.is_exception_date = is_exception_date;
        if (fac_shift)
        {
            shift = "FAC";
            shift_abbr = "F";
            bypass_frequency = "1.0";
            loc_load_plan_shift_cd = "N";
        }
    }

    public String getSic() {
        return sic;
    }

    public String getBeginning_date() {
        return beginning_date;
    }

    public String getEnding_date() {
        return ending_date;
    }

    public String getInstruction_date() {
        return instruction_date;
    }

    public String getPrevious_instruction_date() {
        return previous_instruction_date;
    }

    public boolean isSending_email_ind() {
        return sending_email_ind;
    }

    public boolean isFac_shift() {
        return fac_shift;
    }

    public boolean isIs_exception_date() {
        return is_exception_date;
    }

    public String getShift() {
        return shift;
    }

    public String getShift_abbr() {
        return shift_abbr;
    }

    public String getBypass_frequency() {
        return bypass_frequency;
    }

    public String getLoc_load_plan_shift_cd() {
        return loc_load_plan_shift_cd;
    }
}
