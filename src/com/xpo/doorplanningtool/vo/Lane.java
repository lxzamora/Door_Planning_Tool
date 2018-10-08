package com.xpo.doorplanningtool.vo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Lane {

    String orig_sic;
    String orig_shift;

    String load_to_sic1;
    String must_clear_sic;

    String daylane_freight;
    String load_to_mode2;
    String load_to_sic2;
    String load_to_mode3;
    String load_to_sic3;
    String dest_sic;
    String head_load;
    String bypass;
    double head_load_hit_ratio;
    double head_load_avg_weight;
    double head_load_avg_cube;
    double bypass_hit_ratio;
    double bypass_avg_weight;
    double bypass_avg_cube;
    double avg_weight;
    double avg_cube;

    public Lane(ResultSet rs) throws SQLException {
        orig_sic = rs.getString("orig_sic");
        orig_shift = rs.getString("orig_shift");
        //load_to_mode1 = rs.getString("load_to_mode1");
        //if ( !load_to_mode1.equals('S'))
        //{
        //   load_to_mode1 = "";
        //}
        load_to_sic1 = rs.getString("load_to_sic1");
        must_clear_sic = rs.getString("must_clear_sic");
        // must_clear_shift = rs.getString("must_clear_shift");
        daylane_freight = rs.getString("daylane_freight");
        if (daylane_freight.equals("Y"))
        {
            daylane_freight = "D";
        }
        else
        {
            daylane_freight = "";
        }

        load_to_sic2 = rs.getString("load_to_sic2");
        load_to_sic3 = rs.getString("load_to_sic3");
        dest_sic = rs.getString("dest_sic");
        head_load = rs.getString("head_load");
        bypass = rs.getString("bypass");
        avg_weight = rs.getDouble("avg_weight");
        avg_cube = rs.getDouble("avg_cube");
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

    public String getLoad_to_mode2() {
        return load_to_mode2;
    }

    public void setLoad_to_mode2(String load_to_mode2) {
        this.load_to_mode2 = load_to_mode2;
    }

    public String getLoad_to_sic2() {
        return load_to_sic2;
    }

    public void setLoad_to_sic2(String load_to_sic2) {
        this.load_to_sic2 = load_to_sic2;
    }

    public String getLoad_to_mode3() {
        return load_to_mode3;
    }

    public void setLoad_to_mode3(String load_to_mode3) {
        this.load_to_mode3 = load_to_mode3;
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

    public double getHead_load_hit_ratio() {
        return head_load_hit_ratio;
    }

    public void setHead_load_hit_ratio(double head_load_hit_ratio) {
        this.head_load_hit_ratio = head_load_hit_ratio;
    }

    public double getHead_load_avg_weight() {
        return head_load_avg_weight;
    }

    public void setHead_load_avg_weight(double head_load_avg_weight) {
        this.head_load_avg_weight = head_load_avg_weight;
    }

    public double getHead_load_avg_cube() {
        return head_load_avg_cube;
    }

    public void setHead_load_avg_cube(double head_load_avg_cube) {
        this.head_load_avg_cube = head_load_avg_cube;
    }

    public double getBypass_hit_ratio() {
        return bypass_hit_ratio;
    }

    public void setBypass_hit_ratio(double bypass_hit_ratio) {
        this.bypass_hit_ratio = bypass_hit_ratio;
    }

    public double getBypass_avg_weight() {
        return bypass_avg_weight;
    }

    public void setBypass_avg_weight(double bypass_avg_weight) {
        this.bypass_avg_weight = bypass_avg_weight;
    }

    public double getBypass_avg_cube() {
        return bypass_avg_cube;
    }

    public void setBypass_avg_cube(double bypass_avg_cube) {
        this.bypass_avg_cube = bypass_avg_cube;
    }

    public double getAvg_weight() {
        return avg_weight;
    }

    public void setAvg_weight(double avg_weight) {
        this.avg_weight = avg_weight;
    }

    public double getAvg_cube() {
        return avg_cube;
    }

    public void setAvg_cube(double avg_cube) {
        this.avg_cube = avg_cube;
    }
}
