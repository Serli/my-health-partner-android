package com.serli.myhealthpartner.controller;

import android.content.Context;
import android.content.Intent;

import com.serli.myhealthpartner.AccelerometerService;
import com.serli.myhealthpartner.model.AccelerometerDAO;
import com.serli.myhealthpartner.model.AccelerometerData;
import com.serli.myhealthpartner.model.CompleteData;
import com.serli.myhealthpartner.model.PedometerDAO;
import com.serli.myhealthpartner.model.PedometerData;
import com.serli.myhealthpartner.model.ProfileDAO;

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
    private AccelerometerDAO accDAO;
    private PedometerDAO pedDAO;
    private ProfileDAO proDAO;

    /**
     * Build a new main controller with the given context.
     *
     * @param context The context of the attached view.
     */
    public MainController(Context context) {
        this.context = context;

        accDAO = new AccelerometerDAO(context);
        accDAO.open();

        pedDAO = new PedometerDAO(context);
        pedDAO.open();

        proDAO = new ProfileDAO(context);
        proDAO.open();
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
        final ArrayList<AccelerometerData> accData = accDAO.getData();

        deleteAcquisition();

        ProfileController controllerProfile = new ProfileController(context);

        ArrayList<CompleteData> data = new ArrayList<>();

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.valueOf("http://salquier.pro:80/"))
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

        Call<List<Long>> callData = post.sendData(data);
        callData.enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                if(response.isSuccessful()) {
                    System.out.println("Send Acquisition OK !");
                    System.out.println(response.body());
                    long timestampStart = accData.get(0).getTimestamp();
                    List<Long> activities = response.body();
                    for (int i = 0; i < activities.size(); ++i) {
                        if (activities.get(i) != 0) {
                            PedometerData pedometerData = new PedometerData();
                            pedometerData.setActivity(activities.get(i).intValue());
                            pedometerData.setTimestamp(timestampStart + i * 5000L);
                            pedometerData.setDuration(5000L);
                            if (activities.get(i) == 1) {
                                pedometerData.setDistance(7.0 / 1000.0);
                                pedometerData.setCalories(proDAO.getProfile().getWeight() * pedometerData.getDistance());
                                pedometerData.setSteps(5);
                            }
                            if (activities.get(i) == 2) {
                                pedometerData.setDistance(14.0 / 1000.0);
                                pedometerData.setCalories(proDAO.getProfile().getWeight() * pedometerData.getDistance());
                                pedometerData.setSteps(10);
                            }
                            pedDAO.addEntry(pedometerData);
                        }
                    }
                }
                else {
                    System.out.println("Send Acquisition OK but response unsuccessful " + response.raw());
                    for(AccelerometerData acc : accData)
                        accDAO.addEntry(acc.getX(), acc.getY(), acc.getZ(), acc.getTimestamp(), acc.getActivity());
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                System.out.println("Send Acquisition KO !");
                for(AccelerometerData acc : accData)
                    accDAO.addEntry(acc.getX(), acc.getY(), acc.getZ(), acc.getTimestamp(), acc.getActivity());
                t.printStackTrace();
            }
        });
    }

    /**
     * Delete the stored data.
     */
    public void deleteAcquisition() {
        accDAO.deleteData();
    }

    /**
     * Return the accelerometer data stored in the database.
     * @return The accelerometer data
     */
    public ArrayList<AccelerometerData> getData() {
        ArrayList<AccelerometerData> data = accDAO.getData();
        return data;
    }

    /**
     * Return the calorie burned today by walking.
     * @return the calorie burned
     */
    public float getCalorieWalking() {
        List<PedometerData> pedometerDataList = pedDAO.getTodayPedometer();
        float count = 0;
        for(PedometerData pedometerData : pedometerDataList)
            if(pedometerData.getActivity() == 1)
                count += pedometerData.getCalories();
        return count;
    }

    /**
     * Return the number of steps accomplished today by walking.
     * @return the number of steps
     */
    public int getStepWalking() {
        List<PedometerData> pedometerDataList = pedDAO.getTodayPedometer();
        int count = 0;
        for(PedometerData pedometerData : pedometerDataList)
            if(pedometerData.getActivity() == 1)
                count += pedometerData.getSteps();
        return count;
    }

    /**
     * Return the calorie burned today by running.
     * @return the calorie burned
     */
    public float getCalorieRunning() {
        List<PedometerData> pedometerDataList = pedDAO.getTodayPedometer();
        float count = 0;
        for(PedometerData pedometerData : pedometerDataList)
            if(pedometerData.getActivity() == 2)
                count += pedometerData.getCalories();
        return count;
    }

    /**
     * Return the number of steps accomplished today by running.
     * @return the number of steps
     */
    public int getStepRunning() {
        List<PedometerData> pedometerDataList = pedDAO.getTodayPedometer();
        int count = 0;
        for(PedometerData pedometerData : pedometerDataList)
            if(pedometerData.getActivity() == 2)
                count += pedometerData.getSteps();
        return count;
    }

    /**
     * Return the number of step accomplished today.
     * @return the number of step
     */
    public int getStepDone() {
        List<PedometerData> pedometerDataList = pedDAO.getTodayPedometer();
        int count = 0;
        for(PedometerData pedometerData : pedometerDataList)
            count += pedometerData.getSteps();
        return count;
    }

    /**
     * Return the number of step planned for today.
     * @return the number of step
     */
    public int getStepPlanned() {
        return 10000;
    }

    @Override
    protected void finalize() throws Throwable {
        accDAO.close();
        pedDAO.close();
        proDAO.close();
        super.finalize();
    }
}
