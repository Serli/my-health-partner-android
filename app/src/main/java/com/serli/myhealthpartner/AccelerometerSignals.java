package com.serli.myhealthpartner;



public enum AccelerometerSignals {

    MAGNITUDE,
    MOV_AVERAGE
    ;

    public static final int count = AccelerometerSignals.values().length;
    public static final String[] OPTIONS = {"|V|","\\u0394g"};

}