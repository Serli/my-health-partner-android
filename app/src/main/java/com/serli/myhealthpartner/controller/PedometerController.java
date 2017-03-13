package com.serli.myhealthpartner.controller;

import android.content.Context;

import com.serli.myhealthpartner.model.PedometerDAO;
import com.serli.myhealthpartner.model.PedometerData;

public class PedometerController {

    private PedometerDAO dao;

    private PedometerData pedometer;

    /**
     * Build a new pedometer controller with the given context.
     *
     * @param context The context of the attached view.
     */
    public PedometerController(Context context) {
        dao = new PedometerDAO(context);

        dao.open();

        pedometer = dao.getPedometer();
    }

    public PedometerData getPedometer() {
        if (pedometer == null) {
            pedometer = dao.getPedometer();
        }
        return pedometer;
    }

    public void setPedometer(PedometerData data) {
        dao.addEntry(data);
    }

    /**
     * Close the DAO and release the resources
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        dao.close();
        super.finalize();
    }

}
