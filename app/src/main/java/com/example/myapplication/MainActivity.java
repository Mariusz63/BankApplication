package com.example.myapplication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.animation.core.DurationBasedAnimationSpec;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Adapters.TransactionAdapter;
import com.example.myapplication.Authentication.LoginActivity;
import com.example.myapplication.Authentication.RegisterActivity;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Dialogs.AddTransactionDialog;
import com.example.myapplication.Models.Transaction;
import com.example.myapplication.Models.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private TextView txtAmount, txtWelcome;
    private RecyclerView transactionRecView;
    private BarChart barChart;
    private LineChart lineChart;
    private FloatingActionButton fbAddTransaction;
    private Toolbar toolbar;
    private Utils utils;
    private DatabaseHelper databaseHelper;

    private GetAccountAmount getAccountAmount;
    private TransactionAdapter transactionAdapter;
    private GetTransaction getTransactions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initBottomNavView();

        setSupportActionBar(toolbar);

        utils = new Utils(this);
        User user = utils.isUserLoggedIn();

        if (null != user) {
            Toast.makeText(this, "User: " + user.getFirst_name() + "logged in", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        databaseHelper = new DatabaseHelper(this);

        setupAmount();
        setOnClickListeners();
        initTransactionRecView();
    }

    private void initTransactionRecView() {
        Log.d(TAG, "initTransactionRecView: started");

        transactionAdapter = new TransactionAdapter();
        transactionRecView.setAdapter(transactionAdapter);
        transactionRecView.setLayoutManager(new LinearLayoutManager(this));

        getTransactions();
    }

    private void getTransactions() {
        Log.d(TAG, "getTransactions: started");

        getTransactions = new GetTransaction();
        User user = utils.isUserLoggedIn();
        if(null!=user){
            getTransactions.execute(user.get_id());
        }
    }

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListeners: started");

        txtWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("YourBank")
                        .setMessage("Created and Developed by Mariusz")
                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Visit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, WebsiteActivity.class);
                                startActivity(intent);
                            }
                        });
                builder.show();
            }
        });

        fbAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTransactionDialog addTransactionDialog = new AddTransactionDialog();
                addTransactionDialog.show(getSupportFragmentManager(),"add transaction dialog");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupAmount();
        getTransactions();
    }
    @Override
    protected void onStart() {
        super.onStart();

        setupAmount();
        getTransactions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=getTransactions){
            if(getTransactions.isCancelled()){
                getTransactions.cancel(true);
            }
        }

        if(getAccountAmount!=null){
            if(getAccountAmount.isCancelled()){
                getAccountAmount.cancel(true);
            }
        }
    }

    private void setupAmount() {
        Log.d(TAG, "setupAmount: started");
        User user = utils.isUserLoggedIn();

        if(null!=user){
            getAccountAmount = new GetAccountAmount();
            getAccountAmount.execute(user.get_id());
        }
    }

    private class GetAccountAmount extends AsyncTask<Integer, Void, Double> {

        @Override
        protected Double doInBackground(Integer... integers) {
            try{
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("users", new String[] {"remained_amount"}, "_id=?",
                        new String[] {String.valueOf(integers[0])},null,null,null);

                if(null!=cursor){
                    if(cursor.moveToFirst()){
                        double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("remained_amount"));
                        cursor.close();
                        db.close();
                        return  amount;
                    }else {
                        cursor.close();
                        db.close();
                        return null;
                    }
                }else {
                    db.close();
                    return null;
                }
            }catch (SQLException e){
                e.printStackTrace();
                return null;

            }
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);

            if(aDouble!=null){
                txtAmount.setText(String.valueOf(aDouble)+" $");
            }else {
                txtAmount.setText("0.0 $");
            }
        }
    }

    private class GetTransaction extends AsyncTask<Integer, Void, ArrayList<Transaction>>{

        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("transactions", null,"user_id=?",
                        new String[] {String.valueOf(integers[0])},null,null,"date");
                if(null!=cursor){
                    if(cursor.moveToFirst()){
                        ArrayList<Transaction> transactions = new ArrayList<>();
                        for(int i=0;i < cursor.getCount();i++){
                            Transaction transaction = new Transaction();
                            transaction.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                            transaction.setAmount(cursor.getInt(cursor.getColumnIndexOrThrow("amount")));
                            transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                            transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                            transaction.setRecipient(cursor.getString(cursor.getColumnIndexOrThrow("recipient")));
                            transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                            transaction.setUser_id(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                            transactions.add(transaction);
                            cursor.moveToFirst();

                        }
                        cursor.close();
                        db.close();
                        return transactions;
                    }else{
                        cursor.close();
                        db.close();
                        return null;
                    }
                }else{
                    db.close();
                    return null;
                }
            }catch (SQLException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Transaction> transactions) {
            super.onPostExecute(transactions);

            if(null!=transactions){
                transactionAdapter.setTransactions(transactions);
            }else{
                transactionAdapter.setTransactions(new ArrayList<Transaction>());
            }
        }
    }


    private void initBottomNavView() {
        Log.d(TAG, "initBottomNavView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.menu_item_stats:
                        //TODO: complete this logic
                        break;
                    case R.id.menu_item_transaction:
                        //TODO: complete this logic
                        break;
                    case R.id.menu_item_home:
                        //TODO: complete this logic
                        break;
                    case R.id.menu_item_loan:
                        //TODO: complete this logic
                        break;
                    case R.id.menu_item_investments:
                        //TODO: complete this logic
                        break;
                    default:
                        break;
                }

                return false;
            }
        });
    }

    private void initView(){
        Log.d(TAG, "initView: started");
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
        txtAmount = (TextView) findViewById(R.id.txtAmount);
        txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        transactionRecView = (RecyclerView) findViewById(R.id.transactionRecView);
        barChart = (BarChart) findViewById(R.id.dailySpentChart);
        lineChart = (LineChart) findViewById(R.id.profitChart);
        fbAddTransaction = (FloatingActionButton) findViewById(R.id.fbAddTransaction);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }
}



/*   for test:
     DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query("items",null,null,null,null,null,null);

        if(null!=cursor)
        {
             Log.d(TAG, "onCreate: name:" + cursor.getString(cursor.getColumnIndex("name")));
        }*/

