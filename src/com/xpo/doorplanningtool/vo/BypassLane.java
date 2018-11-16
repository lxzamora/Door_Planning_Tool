package com.xpo.doorplanningtool.vo;

//class for columns to insert in FFO_DOOR_PLANNING_WRKBK_V1 in PRD_CWFENG

public class BypassLane {
    String updt_tmst;
    String instruction_dt;
    String orig_sic;
    String orig_shift;
    String load_to_sic1;
    String must_clear_sic;
    String daylane_freight;
    String load_to_sic2;
    String load_to_sic3;
    String dest_sic;
    String head_load;
    String bypass;

    public BypassLane(String updt_tmst, String instruction_dt, String orig_sic, String orig_shift, String load_to_sic1, String must_clear_sic, String daylane_freight, String load_to_sic2, String load_to_sic3, String dest_sic, String head_load, String bypass) {
        this.updt_tmst = updt_tmst;
        this.instruction_dt = instruction_dt;
        this.orig_sic = orig_sic;
        this.orig_shift = orig_shift;
        this.load_to_sic1 = load_to_sic1;
        this.must_clear_sic = must_clear_sic;
        this.daylane_freight = daylane_freight;
        this.load_to_sic2 = load_to_sic2;
        this.load_to_sic3 = load_to_sic3;
        this.dest_sic = dest_sic;
        this.head_load = head_load;
        this.bypass = bypass;
    }

    public String getUpdt_tmst() {
        return updt_tmst;
    }

    public void setUpdt_tmst(String updt_tmst) {
        this.updt_tmst = updt_tmst;
    }

    public String getInstruction_dt() {
        return instruction_dt;
    }

    public void setInstruction_dt(String instruction_dt) {
        this.instruction_dt = instruction_dt;
    }

    public String getOrig_sic() {
        return orig_sic;
    }

    public void setOrig_sic(String orig_sic) {
        this.orig_sic = orig_sic;
    }

    public String getOrig_shift() {
        return orig_shift;
    }

    public void setOrig_shift(String orig_shift) {
        this.orig_shift = orig_shift;
    }

    public String getLoad_to_sic1() {
        return load_to_sic1;
    }

    public void setLoad_to_sic1(String load_to_sic1) {
        this.load_to_sic1 = load_to_sic1;
    }

    public String getMust_clear_sic() {
        return must_clear_sic;
    }

    public void setMust_clear_sic(String must_clear_sic) {
        this.must_clear_sic = must_clear_sic;
    }

    public String getDaylane_freight() {
        return daylane_freight;
    }

    public void setDaylane_freight(String daylane_freight) {
        this.daylane_freight = daylane_freight;
    }

    public String getLoad_to_sic2() {
        return load_to_sic2;
    }

    public void setLoad_to_sic2(String load_to_sic2) {
        this.load_to_sic2 = load_to_sic2;
    }

    public String getLoad_to_sic3() {
        return load_to_sic3;
    }

    public void setLoad_to_sic3(String load_to_sic3) {
        this.load_to_sic3 = load_to_sic3;
    }

    public String getDest_sic() {
        return dest_sic;
    }

    public void setDest_sic(String dest_sic) {
        this.dest_sic = dest_sic;
    }

    public String getHead_load() {
        return head_load;
    }

    public void setHead_load(String head_load) {
        this.head_load = head_load;
    }

    public String getBypass() {
        return bypass;
    }

    public void setBypass(String bypass) {
        this.bypass = bypass;
    }

}
