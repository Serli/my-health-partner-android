package com.serli.myhealthpartner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.serli.myhealthpartner.controller.ProfileController;
import com.serli.myhealthpartner.model.ProfileData;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by electroplanet on 28/02/2017.
 */

public class RepportActivity extends AppCompatActivity {

    private ProfileController controller;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repport);
       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
*/
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

}
