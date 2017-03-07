package com.serli.myhealthpartner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.serli.myhealthpartner.controller.ProfileController;
import com.serli.myhealthpartner.model.ProfileData;

import java.util.ArrayList;
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
        setContentView(R.layout.test);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        Typeface font = Typeface.createFromAsset(getAssets(), "Poppins-Regular.ttf");
        Typeface fontSemiBold = Typeface.createFromAsset(getAssets(), "Poppins-SemiBold.ttf");

        PieChart podometerPieChart = (PieChart) findViewById(R.id.piechart_podometer);
        podometerPieChart.setHoleColor(Color.argb(255, 64, 134, 237));

        podometerPieChart.setCenterTextTypeface(font);
        podometerPieChart.setCenterTextColor(Color.argb(255, 255, 255, 255));

        ArrayList<PieEntry> podometerEntries = new ArrayList<>();
        PieEntry podometerNow, podometerGoal;

        podometerNow = new PieEntry(500, 0);
        podometerGoal = new PieEntry(1000, 1);
        podometerPieChart.setCenterText("Nombre de pas : " + Math.round(podometerNow.getValue()) + "/" + Math.round(podometerGoal.getValue() + podometerNow.getValue()));

        Description descrip = new Description();
        descrip.setText("");
        podometerPieChart.setDescription(descrip);
        podometerPieChart.getLegend().setEnabled(false);

        podometerEntries.add(podometerNow);
        podometerEntries.add(podometerGoal);

        PieDataSet dataset = new PieDataSet(podometerEntries, "Podometer");
        dataset.setColors(Color.argb(255, 255, 255, 255), Color.argb(255, 255, 193, 9));

        PieData data = new PieData(dataset);
        podometerPieChart.setData(data);
        data.setValueTypeface(fontSemiBold);
        data.setValueTextSize(15f);

        TextView textViewWalkingTodayCalories = (TextView) findViewById(R.id.textView_walking_today_calories);
        TextView textViewRunningTodayCalories = (TextView) findViewById(R.id.textView_running_today_calories);

        textViewWalkingTodayCalories.setText("345 kcal");
        textViewRunningTodayCalories.setText("564 kcal");

        textViewWalkingTodayCalories.setTypeface(fontSemiBold);
        textViewRunningTodayCalories.setTypeface(fontSemiBold);

        TextView textViewWalkingTodaySteps = (TextView) findViewById(R.id.textView_walking_today_steps);
        TextView textViewRunningTodaySteps = (TextView) findViewById(R.id.textView_running_today_steps);

        textViewWalkingTodaySteps.setText("312 pas");
        textViewRunningTodaySteps.setText("188 pas");

        textViewWalkingTodaySteps.setTypeface(font);
        textViewRunningTodaySteps.setTypeface(font);
    }

}
