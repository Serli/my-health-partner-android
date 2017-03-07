package com.serli.myhealthpartner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.serli.myhealthpartner.controller.ProfileController;

public class PowerBroadcastReceiver extends BroadcastReceiver {
    private ProfileController profileController;

    @Override
    public void onReceive(Context context, Intent intent) {
        profileController = new ProfileController(context);
        if (profileController.getProfile() != null){
            Intent myIntent = new Intent(context, AccelerometerService.class);
            context.startService(myIntent);
        }
    }
}