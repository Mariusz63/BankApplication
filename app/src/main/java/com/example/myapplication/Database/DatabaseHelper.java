package com.example.myapplication.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.database.ContentObservable;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG ="DatabaseHelper";
    private static final String DB_Name = "Db_bank";
    private static final int Db_Version =1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_Name, null, Db_Version);
    }


    @Override
    public void onCreate(SQLiteDatabase dataBase) {
        Log.d(TAG, "onCreate: started");
        String createUserTable ="CREATE TABLE users (_id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT NOT NULL, password TEXT NOT NULL, " +
        "first_name TEXT, last_name TEXT, address TEXT, image_url TEXT, remained_amount DOUBLE)";

        String createShoppingTable = "CREATE TABLE shopping (_id INTEGER PRIMARY KEY AUTOINCREMENT, item_id INTEGER, " +
        "user_id INTEGER, transaction_id INTEGER, price DOUBLE, date DATE, description TEXT)";

        String createInvestmentTable = "CREATE TABLE investments (_id INTEGER PRIMARY KEY, amount DOUBLE, " +
                "monthly_roi DOUBLE, name TEXT, init_date DATE, finish_date DATE, user_id INTEGER, transaction_id INTEGER)";

        String createLoansTable = "CREATE TABLE loans (_id INTEGER PRIMARY KEY, init_date DATE, "+
                "finish_date DATE, init_amount DOUBLE, remained_amount DOUBLE, monthly_payment DOUBLE, monthly_roi DOUBLE,"+
                "name TEXT, user_id INTEGER)";

        String createTransactionTable = "CREATE TABLE transactions (_id INTEGER PRIMARY KEY, amount DOUBLE,"+
                "date DATE, type TEXT, user_id INTEGER, recipient TEXT, description TEXT)";

        String createItemsTable = "CREATE TABLE items (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, image_url TEXT,"+
                "description TEXT)";

        dataBase.execSQL(createUserTable);
        dataBase.execSQL(createShoppingTable);
        dataBase.execSQL(createInvestmentTable);
        dataBase.execSQL(createLoansTable);
        dataBase.execSQL(createTransactionTable);
        dataBase.execSQL(createItemsTable);

        addInitialItems(dataBase);
    }

    private void addInitialItems(SQLiteDatabase db){
    Log.d(TAG,"addInitialItems: started");
        ContentValues values = new ContentValues();
        values.put("name", "Bike");
        values.put("image_url", "https://voltbikes.co.uk/e-bikes/hybrid/pulse");
        values.put("description", "The perfect mountain bike!");

        db.insert("items",null,values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
