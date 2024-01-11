package com.example.myapplication.Database;

import android.content.ContentValues;
import android.content.Context;
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

        String createUserLoginHistoryTable = "CREATE TABLE user_login_history ( history_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER,login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, login_location TEXT, FOREIGN KEY(user_id) REFERENCES users(_id))";

        String createConversionTable = "CREATE TABLE conversions (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, base_currency TEXT, converted_currency TEXT, " +
                "amount DOUBLE, conversion_rate DOUBLE, result DOUBLE, date DATE, " +
                "FOREIGN KEY(user_id) REFERENCES users(_id))";


        dataBase.execSQL(createUserTable);
        dataBase.execSQL(createShoppingTable);
        dataBase.execSQL(createInvestmentTable);
        dataBase.execSQL(createLoansTable);
        dataBase.execSQL(createTransactionTable);
        dataBase.execSQL(createItemsTable);
        dataBase.execSQL(createUserLoginHistoryTable);
        dataBase.execSQL(createConversionTable);

        addInitialItems(dataBase);
        addTestTransaction(dataBase);
        addTestProfit(dataBase);
        addTestShopping(dataBase);
    }

    private void addTestShopping(SQLiteDatabase dataBase) {
        Log.d(TAG, "addTestShopping: started");

        ContentValues firstValues = new ContentValues();
        firstValues.put("item_id",1);
        firstValues.put("transaction_id",1);
        firstValues.put("user_id",1);
        firstValues.put("price",69.69);
        firstValues.put("description","some description");
        firstValues.put("date","2023-07-19");
        dataBase.insert("shopping",null,firstValues);

        ContentValues secondValue = new ContentValues();
        secondValue.put("item_id",1);
        secondValue.put("transaction_id",1);
        secondValue.put("user_id",1);
        secondValue.put("price",69.69);
        secondValue.put("description","some description");
        secondValue.put("date","2023-07-19");
        dataBase.insert("shopping",null,secondValue);
    }

    private void addTestProfit(SQLiteDatabase dataBase) {
        Log.d(TAG, "addTestProfit: started");

        ContentValues firstValues = new ContentValues();
        firstValues.put("amount", 99.0);
        firstValues.put("type", "profit");
        firstValues.put("date", "2023-02-22");
        firstValues.put("description", "Profit of work");
        firstValues.put("user_id", 1);
        firstValues.put("recipient", "Januszex");
        dataBase.insert("transactions",null,firstValues);

        ContentValues secondValues = new ContentValues();
        secondValues.put("amount", 10.3);
        secondValues.put("type", "profit");
        secondValues.put("date", "2022-03-22");
        secondValues.put("description", "Profit ");
        secondValues.put("user_id", 1);
        secondValues.put("recipient", "Januszex2");
        dataBase.insert("transactions",null,secondValues);

        ContentValues thirdValues = new ContentValues();
        thirdValues.put("amount", 10.3);
        thirdValues.put("type", "profit");
        thirdValues.put("date", "2022-06-22");
        thirdValues.put("description", "Profit ");
        thirdValues.put("user_id", 1);
        thirdValues.put("recipient", "Januszex3");
        dataBase.insert("transactions",null,thirdValues);


    }

    private void addTestTransaction(SQLiteDatabase dataBase) {
        Log.d(TAG, "addTextTransaction: started");
        ContentValues values = new ContentValues();
        values.put("_id",0);
        values.put("amount",33.3);
        values.put("date","2023-01-11");
        values.put("type","shopping");
        values.put("user_id",1);
        values.put("description","The best phone ever");
        values.put("recipient","MediaExpert");
        long newTransactionId = dataBase.insert("transactions",null,values);
        Log.d(TAG, "addTestTransaction: transaction id" + newTransactionId);
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
