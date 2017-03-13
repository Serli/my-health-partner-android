package com.serli.myhealthpartner.controller;

import android.content.Context;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.serli.myhealthpartner.model.AccelerometerData;
import com.serli.myhealthpartner.model.ProfileDAO;
import com.serli.myhealthpartner.model.ProfileData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    public ProfileData getProfile() {
        if (profile == null) {
            profile = dao.getProfile();
        }
        return profile;
    }

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
