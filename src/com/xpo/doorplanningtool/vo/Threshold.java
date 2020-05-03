package com.xpo.doorplanningtool.vo;

public class Threshold {

    String max_cube_out;        //as of October 14, 2018 max_cube_out = 1446
    String max_weight_out;      //as of October 14, 2018 max_weight_out = 16,000
    String bypass_threshold;    //as of October 14, 2018 bypass_threshold = 1.0
    String headload_threshold;    //as of October 14, 2018 headload_threshold = 0.4

    public String getMax_cube_out() {
        return max_cube_out;
    }

    public void setMax_cube_out(String max_cube_out) {
        this.max_cube_out = max_cube_out;
    }

    public String getMax_weight_out() {
        return max_weight_out;
    }

    public void setMax_weight_out(String max_weight_out) {
        this.max_weight_out = max_weight_out;
    }

    public String getBypass_threshold() {
        return bypass_threshold;
    }

    public void setBypass_threshold(String bypass_threshold) {
        this.bypass_threshold = bypass_threshold;
    }

    public String getHeadload_threshold() {
        return headload_threshold;
    }

    public void setHeadload_threshold(String headload_threshold) {
        this.headload_threshold = headload_threshold;
    }
}
