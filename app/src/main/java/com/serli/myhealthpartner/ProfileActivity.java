package com.serli.myhealthpartner;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.serli.myhealthpartner.controller.ProfileController;
import com.serli.myhealthpartner.model.ProfileData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * View of Profile Activity.<br/>
 * allow the user to Inform his profile (gender, Birthday, Height and weight)<br/>
 * this information will be used to Calculates calories burned <br/>
 */
public class ProfileActivity extends AppCompatActivity {

    private ProfileController controller;

    private EditText editTextHeight;
    private EditText editTextWeight;
    private EditText editTextBirthday;
    ProfileData profile;
    int genderChoice;
    Calendar c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        controller = new ProfileController(this);
        profile = new ProfileData();

        genderChoice = -1;
        editTextHeight = (EditText) findViewById(R.id.editText_height);
        editTextWeight = (EditText) findViewById(R.id.editText_weight);
        editTextBirthday = (EditText) findViewById(R.id.editText_birthday);

        TextView textViewProfile = (TextView) findViewById(R.id.profile_information);
        final Button buttonGenderFemale = (Button) findViewById(R.id.gender_button_female);
        final Button buttonGenderMale = (Button) findViewById(R.id.gender_button_male);
        final Button buttonSignUp = (Button) findViewById(R.id.signup_button);

        Typeface font = Typeface.createFromAsset(getAssets(), "Poppins-Regular.ttf");
        Typeface fontBold = Typeface.createFromAsset(getAssets(), "Poppins-SemiBold.ttf");

        editTextHeight.setTypeface(font);
        editTextWeight.setTypeface(font);
        editTextBirthday.setTypeface(font);
        textViewProfile.setTypeface(fontBold);
        buttonGenderFemale.setTypeface(font);
        buttonGenderMale.setTypeface(font);
        buttonSignUp.setTypeface(font);

        if (controller.getProfile() != null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            profile = controller.getProfile();

            if (profile.getGender() == 0){
                buttonGenderMale.setBackground(getResources().getDrawable(R.drawable.signup_selected_button));
                buttonGenderMale.setTextColor(getResources().getColor(R.color.blueBackground));
                genderChoice = 0;
            }
            else if(profile.getGender() == 1){
                buttonGenderFemale.setBackground(getResources().getDrawable(R.drawable.signup_selected_button));
                buttonGenderFemale.setTextColor(getResources().getColor(R.color.blueBackground));
                genderChoice = 1;
            }

            editTextHeight.setText(String.valueOf(profile.getHeight()) + " cm");
            editTextWeight.setText(String.valueOf(profile.getWeight()) + " kg");

            Date d = profile.getBirthday();
            c = Calendar.getInstance();
            c.setTime(d);
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
            editTextBirthday.setText(sdf.format(c.getTime()));
        }

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextHeight.getText().length() > 0 && editTextWeight.getText().length() > 0 && genderChoice > -1 && c != null) {
                    profile.setId_profile(profile.getId_profile() + 1);
                    profile.setGender(genderChoice);
                    profile.setHeight(Integer.parseInt(editTextHeight.getText().toString()));
                    profile.setWeight(Integer.parseInt(editTextWeight.getText().toString()));

                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH);

                    Calendar calendarBirthday = Calendar.getInstance();
                    calendarBirthday.set(year, month, day);

                    profile.setBirthday(calendarBirthday.getTime());

                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    telephonyManager.getDeviceId();

                    profile.setIMEI(Long.parseLong(telephonyManager.getDeviceId()));

                    controller.setProfile(profile);
                    Intent myIntent = new Intent(ProfileActivity.this, AccelerometerService.class);
                    ProfileActivity.this.startService(myIntent);
                    finish();
                }
                else{
                    Toast.makeText(ProfileActivity.this, R.string.invalid_profile,Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonGenderFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(genderChoice == 0 || genderChoice == -1){
                    buttonGenderFemale.setBackground(getResources().getDrawable(R.drawable.signup_selected_button));
                    buttonGenderFemale.setTextColor(getResources().getColor(R.color.blueBackground));
                    buttonGenderMale.setBackground(getResources().getDrawable(R.drawable.signup_unselected_button));
                    buttonGenderMale.setTextColor(getResources().getColor(R.color.white));
                    genderChoice = 1;
                }
            }
        });

        buttonGenderMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (genderChoice == 1 || genderChoice == -1) {
                    buttonGenderMale.setBackground(getResources().getDrawable(R.drawable.signup_selected_button));
                    buttonGenderMale.setTextColor(getResources().getColor(R.color.blueBackground));
                    buttonGenderFemale.setBackground(getResources().getDrawable(R.drawable.signup_unselected_button));
                    buttonGenderFemale.setTextColor(getResources().getColor(R.color.white));
                    genderChoice = 0;
                }
            }
        });

        editTextBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog=new DatePickerDialog(ProfileActivity.this, R.style.MyDatePickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                        c.set(Calendar.YEAR, selectedYear);
                        c.set(Calendar.MONTH, selectedMonth);
                        c.set(Calendar.DAY_OF_MONTH, selectedDay);
                        String myFormat = "dd/MM/yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
                        editTextBirthday.setText(sdf.format(c.getTime()));
                    }
                },mYear, mMonth, mDay);
                dialog.setTitle(R.string.select_date);
                dialog.show();
            }
        });

    }

    @Override
    public void onBackPressed(){
        if (controller.getProfile() == null) {
            Toast.makeText(ProfileActivity.this, R.string.profile_quit_forbidden,Toast.LENGTH_LONG).show();
        }
        else{
            ProfileActivity.super.onBackPressed();
        }
    }
}
