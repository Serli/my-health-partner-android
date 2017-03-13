package com.serli.myhealthpartnerdevelopper.controller;

import android.content.Context;

import com.serli.myhealthpartnerdevelopper.model.ProfileDAO;
import com.serli.myhealthpartnerdevelopper.model.ProfileData;

/**
 * Controller for the profile.
 */
public class ProfileController {

    private ProfileDAO dao;

    private ProfileData profile;

    /**
     * Build a new profile controller with the given context.
     *
     * @param context The context of the attached view.
     */
    public ProfileController(Context context) {
        dao = new ProfileDAO(context);

        dao.open();

        profile = dao.getProfile();
    }

    /**
     * @return The profile stored in the database or null if none exist.
     */
    public ProfileData getProfile() {
        if (profile == null) {
            profile = dao.getProfile();
        }
        return profile;
    }

    /**
     * Set the profile in the database.
     * @param data The {@link ProfileData} containing the profile.
     */
    public void setProfile(ProfileData data) {
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
