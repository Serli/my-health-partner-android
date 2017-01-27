package com.serli.myhealthpartner;

/**
 * Created by kahina on 26/01/2017.
 */

public enum AccelerometerSignals {

    MAGNITUDE,
    MOV_AVERAGE
    ;

    public static final int count = AccelerometerSignals.values().length;
    public static final String[] OPTIONS = {"|V|","\\u0394g"};

}
