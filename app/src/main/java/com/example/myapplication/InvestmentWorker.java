package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.Database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class InvestmentWorker extends Worker {
    private static final String TAG = "InvestmentWorker";
    private DatabaseHelper databaseHelper;
    public InvestmentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {

        Log.d(TAG, "doWork: called");

        Data data = getInputData();
        double amount = data.getDouble("amount",0.0);
        String recipient = data.getString("recipient");
        String description = data.getString("description");
        int user_id = data.getInt("user_id",-1);
        String type = "profit";

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(calendar.getTime());

        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("recipient", recipient);
        values.put("description", description);
        values.put("user_id", user_id);
        values.put("type", type);
        values.put("date", date);

        try{
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
           long id = db.insert("transactions", null,values);

           if(id!=-1){
               Cursor cursor = db.query("users", new String[] {"remained_amount"}, "id=?", new String[] {String.valueOf(user_id)}, null,null,null);

               if(cursor!=null){
                   if(cursor.moveToFirst()){
                       double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("remained_amount"));
                       ContentValues newValues = new ContentValues();
                       newValues.put("remained_amount", currentRemainedAmount - amount);
                       int affectedRows = db.update("users", newValues, "_id=?", new String[] {String.valueOf(user_id)});
                       Log.d(TAG, "doInBackground: updateRows: " + affectedRows);
                       cursor.close();
                   }else {
                       cursor.close();
                   }
               }
           }
            db.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
            return Result.failure();
        }

        return Result.success();
    }
}
