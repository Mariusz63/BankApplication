package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.myapplication.Adapters.LoanAdapter;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Models.Loan;
import com.example.myapplication.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class LoanActivity extends AppCompatActivity {
    private static final String TAG = "LoanActivity";

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;

    private LoanAdapter adapter;
    private DatabaseHelper databaseHelper;
    private GetLoans getLoans;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan);

        initViews();
        initBottomNavView();

        adapter = new LoanAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();

        if(user!=null){
            getLoans = new GetLoans();
            getLoans.execute(user.get_id());
        }
    }

    private class GetLoans extends AsyncTask<Integer,Void, ArrayList<Loan>>{

        @Override
        protected ArrayList<Loan> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("loans", null, "user_id=?",
                        new String[] {String.valueOf(integers[0])}, null,null,"init_date DESC");

                if(cursor!=null){
                    if(cursor.moveToNext()){
                        ArrayList<Loan> loans = new ArrayList<>();
                        for(int i = 0; i< cursor.getCount();i++){
                            Loan loan = new Loan();
                            loan.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                            loan.setUser_id(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                            loan.setTransaction_id(cursor.getInt(cursor.getColumnIndexOrThrow("transaction_id")));
                            loan.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                            loan.setInit_date(cursor.getString(cursor.getColumnIndexOrThrow("init_date")));
                            loan.setFinish_date(cursor.getString(cursor.getColumnIndexOrThrow("finish_date")));
                            loan.setInit_amount(cursor.getDouble(cursor.getColumnIndexOrThrow("init_amount")));
                            loan.setMonthly_roi(cursor.getDouble(cursor.getColumnIndexOrThrow("monthly_roi")));
                            loan.setMonthly_payment(cursor.getDouble(cursor.getColumnIndexOrThrow("monthly_payment")));
                            loan.setRemained_amount(cursor.getDouble(cursor.getColumnIndexOrThrow("remained_amount")));
                            loans.add(loan);
                            cursor.moveToNext();
                        }
                        cursor.close();
                        db.close();
                        return loans;

                    }else {
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
        protected void onPostExecute(ArrayList<Loan> loans) {
            super.onPostExecute(loans);

            if(loans!=null){
                adapter.setLoans(loans);
            }else{
                adapter.setLoans(new ArrayList<Loan>());
            }
        }
    }

    private void initViews() {
        Log.d(TAG, "initView: started");
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
        recyclerView = (RecyclerView) findViewById(R.id.loanRecView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(getLoans!=null){
            if(!getLoans.isCancelled()){
                getLoans.cancel(true);
            }
        }
    }

    private void initBottomNavView() {
        Log.d(TAG, "initBottomNavView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_loan);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.menu_item_stats:
                        Intent statsIntent = new Intent(LoanActivity.this, StatsActivity.class);
                        statsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(statsIntent);
                        break;
                    case R.id.menu_item_transaction:
                        Intent transactionHome = new Intent(LoanActivity.this, TransactionActivity.class);
                        transactionHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(transactionHome);

                        break;
                    case R.id.menu_item_home:
                        Intent intentHome = new Intent(LoanActivity.this, MainActivity.class);
                        intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentHome);
                        break;
                    case R.id.menu_item_loan:
                        break;
                    case R.id.menu_item_investments:
                        Intent investmentIntent = new Intent(LoanActivity.this, InvestmentActivity.class);
                        investmentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(investmentIntent);
                        break;
                    default:
                        break;
                }

                return false;
            }
        });
    }

}