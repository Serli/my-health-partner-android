package com.serli.myhealthpartner.model;

import java.util.Date;

/**
 * Created by kahina on 01/02/2017.
 */

public class PodometreData {



    private int steps;
    private int tstmp;
    private Date date;


    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getTstmp() {
        return tstmp;
    }

    public void setTstmp(int tstmp) {
        this.tstmp = tstmp;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
