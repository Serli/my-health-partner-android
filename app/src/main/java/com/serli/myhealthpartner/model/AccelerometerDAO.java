package com.serli.myhealthpartner.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Link between the acceleremoter data table in the {@link Database} and the controllers.
 */
public class AccelerometerDAO {

    private Database database;
    private SQLiteDatabase db;
    private String[] allColumns = {Database.ACC_TIMESTAMP, Database.ACC_X, Database.ACC_Y, Database.ACC_Z, Database.ACC_ACTIVITY};

    /**
     * @param context The context where the dao is called
     */
    public AccelerometerDAO(Context context) {
        this.database = new Database(context);
    }

    /**
     * Open the connection with the database.
     */
    public void open() {
        db = database.getWritableDatabase();
    }

    /**
     * Close the connection with the database.
     */
    public void close() {
        database.close();
    }

    /**
     * Add an entry in the accelerometer data table.<br/>
     *
     * @param x         Data's X position
     * @param y         Data's Y position
     * @param z         Data's Z position
     * @param timestamp Data's timestamp
     * @param activity  Data's activity
     */
    public void addEntry(float x, float y, float z, long timestamp, int activity) {
        ContentValues values = new ContentValues();
        values.put(Database.ACC_TIMESTAMP, timestamp);
        values.put(Database.ACC_X, x);
        values.put(Database.ACC_Y, y);
        values.put(Database.ACC_Z, z);
        values.put(Database.ACC_ACTIVITY, activity);

        db.insert(Database.ACC_TABLE, null, values);
    }

    /**
     * Return a List of {@link AccelerometerData} object containing the accelerometer data.
     *
     * @return The accelerometer data.
     */
    public ArrayList<AccelerometerData> getData() {

        ArrayList<AccelerometerData> acc_data_list = new ArrayList<>();
        Cursor cursor = db.query(Database.ACC_TABLE,
                allColumns, null, null, null, null, Database.ACC_TIMESTAMP + " DESC");
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            AccelerometerData acc_data = cursorToData(cursor);
            acc_data_list.add(acc_data);
            cursor.moveToNext();
        }
        return acc_data_list;
    }

    /**
     * Delete the accelerometer data in the accelerometer data table
     */
    public void deleteData() {
        db.delete(Database.ACC_TABLE, null, null);
    }

    private AccelerometerData cursorToData(Cursor cursor) {
        AccelerometerData acc_data = new AccelerometerData();
        acc_data.setTimestamp(cursor.getLong(0));
        acc_data.setX(cursor.getFloat(1));
        acc_data.setY(cursor.getFloat(2));
        acc_data.setZ(cursor.getFloat(3));
        acc_data.setActivity(cursor.getInt(4));

        return acc_data;
    }
}
