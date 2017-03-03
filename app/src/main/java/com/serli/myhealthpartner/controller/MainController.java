package com.serli.myhealthpartner.controller;

import android.content.Context;
import android.content.Intent;

import com.serli.myhealthpartner.AccelerometerService;
import com.serli.myhealthpartner.R;
import com.serli.myhealthpartner.model.AccelerometerDAO;
import com.serli.myhealthpartner.model.AccelerometerData;
import com.serli.myhealthpartner.model.CompleteData;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Controller of the main view.
 */
public class MainController {

    private Context context;
    private AccelerometerDAO dao;

    /**
     * Build a new main controller with the given context.
     *
     * @param context The context of the attached view.
     */
    public MainController(Context context) {
        this.context = context;

        dao = new AccelerometerDAO(context);

        dao.open();
    }

    /**
     * Start the acquisition of the accelerometer data.
     * It will start the service {@link AccelerometerService} with the given parameter.
     */
    public void startAcquisition() {
        if (!AccelerometerService.isRunning()) {
            Intent intent = new Intent(context, AccelerometerService.class);
            context.startService(intent);
        }
    }

    /**
     * Send the stored data to the server, then delete it.
     */
    public void sendAcquisition() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.valueOf("http://192.168.42.165:8080/"))
                .addConverterFactory(new Converter.Factory() {
                    @Override
                    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                        final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
                        return new Converter<ResponseBody, Object>() {
                            @Override
                            public Object convert(ResponseBody body) throws IOException {
                                if (body.contentLength() == 0) return null;
                                return delegate.convert(body);                }
                        };
                    }
                })
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PostTo post = retrofit.create(PostTo.class);
        ProfileController controllerProfile = new ProfileController(context);

        ArrayList<CompleteData> data = new ArrayList<>();

        ArrayList<AccelerometerData> accData = dao.getData();

        Calendar curr = Calendar.getInstance();
        Calendar birth = Calendar.getInstance();

        birth.setTime(controllerProfile.getProfile().getBirthday());
        int age = curr.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        curr.add(Calendar.YEAR,-age);
        if(birth.after(curr))
        {
            age = age - 1;
        }

        for (int i = 0; i < accData.size(); i++) {
            CompleteData cd = new CompleteData();
            cd.setHeight(controllerProfile.getProfile().getHeight());
            cd.setWeight(controllerProfile.getProfile().getWeight());
            cd.setImei(controllerProfile.getProfile().getIMEI());

            cd.setAge(age);
            cd.setGender(controllerProfile.getProfile().getGender());
            cd.setTimestamp(accData.get(i).getTimestamp());
            cd.setX(accData.get(i).getX());
            cd.setY(accData.get(i).getY());
            cd.setZ(accData.get(i).getZ());
            cd.setActivity(accData.get(i).getActivity());
            data.add(cd);
        }

        deleteAcquisition();

        Call<List<Long>> callData = post.sendData(data);
        callData.enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                System.out.println("Send Acquisition OK !");
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                System.out.println("Send Acquisition KO !");
                t.printStackTrace();
            }
        });
    }

    /**
     * Delete the stored data.
     */
    public void deleteAcquisition() {
        dao.deleteData();
    }

    public ArrayList<AccelerometerData> getData() {
        ArrayList<AccelerometerData> data = dao.getData();
        return data;
    }

    @Override
    protected void finalize() throws Throwable {
        dao.close();
        super.finalize();
    }
}
