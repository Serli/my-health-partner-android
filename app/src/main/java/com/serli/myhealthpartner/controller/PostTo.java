package com.serli.myhealthpartner.controller;

import com.serli.myhealthpartner.model.CompleteData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Communication mobile to server
 */
public interface PostTo {
    @POST("/recognize")
    Call<List<Long>> sendData (@Body ArrayList<CompleteData> data);
}
