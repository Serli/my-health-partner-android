package com.serli.myhealthpartner.controller;

import com.serli.myhealthpartner.model.AccelerometerData;
import com.serli.myhealthpartner.model.CompleteData;
import com.serli.myhealthpartner.model.ProfileData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by nathan on 21/12/16.
 */

/**
 * Communication mobile to server
 */
public interface PostTo {
    @POST("/data")
    Call<ArrayList<CompleteData>> sendData (@Body ArrayList<CompleteData> data);
}
