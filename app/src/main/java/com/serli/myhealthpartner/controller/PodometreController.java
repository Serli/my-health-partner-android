package com.serli.myhealthpartner.controller;

import android.content.Context;

import com.serli.myhealthpartner.model.PodometreDAO;
import com.serli.myhealthpartner.model.PodometreData;


public class PodometreController {

    private PodometreDAO dao;

    private PodometreData podometre;

    /**
     * Build a new podometre controller with the given context.
     *
     * @param context The context of the attached view.
     */

    public PodometreController(Context context) {
        dao = new PodometreDAO(context);

        dao.open();

        podometre = dao.getPodometre();
    }

    /**
     * @return The podometre stored in the database or null if none exist.
     */

    public PodometreData getPodometre() {
        if(podometre == null){
            podometre = dao.getPodometre();
        }
                return podometre;
    }

    /**
     * Set the podometre in the database.
     * @param data The {@link PodometreData} containing the podometre.
     */

    public void setPodometre (PodometreData data) {
        dao.addEntry(data);
    }

    @Override
    protected void finalize() throws Throwable {
        dao.close();
        super.finalize();
    }

}
