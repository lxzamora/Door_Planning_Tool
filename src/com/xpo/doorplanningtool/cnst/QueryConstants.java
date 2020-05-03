package com.xpo.doorplanningtool.cnst;

public enum QueryConstants {
    NETEZZA_USERNAME		("LXZAMORA"),
    NETEZZA_PASSWORD		("liza_082416"),
    NETEZZA_PRDCWFENG_URL   ("jdbc:netezza://npsdwh.con-way.com/PRD_CWFENG"),
    NETEZZA_PRDWHSEVW_URL   ("jdbc:netezza://npsdwh.con-way.com/PRD_WHSEVIEW?allowMultiQuery=true"),
    ;

    String value = null;
    private QueryConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
