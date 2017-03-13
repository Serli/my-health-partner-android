package com.serli.myhealthpartnerdevelopper.controller;

import com.serli.myhealthpartnerdevelopper.model.CompleteData;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by nathan on 21/12/16.
 */

/**
 * Interface which permits smarphone to send data to the server
 */
public interface PostTo {
    @POST("/data")
    Call<ArrayList<CompleteData>> sendData (@Body ArrayList<CompleteData> data);
}
