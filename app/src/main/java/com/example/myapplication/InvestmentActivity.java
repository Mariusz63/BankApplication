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

import com.example.myapplication.Adapters.InvestmentAdapter;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Models.Investment;
import com.example.myapplication.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class InvestmentActivity extends AppCompatActivity {

    private static final String TAG = "InvestmentActivity";
    private RecyclerView recycleView;
    private BottomNavigationView bottomNavigationView;

    private InvestmentAdapter adapter;
    private DatabaseHelper databaseHelper;
    private GetInvestment getInvestment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);

        initViews();
        initBottomNavView();

        adapter = new InvestmentAdapter(this);
        recycleView.setAdapter(adapter);
        recycleView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper =new DatabaseHelper(this);

        getInvestment = new GetInvestment();
        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();

        if(user!=null){
            getInvestment.execute(user.get_id());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != getInvestment) {
            if (!getInvestment.isCancelled()) {
                getInvestment.cancel(true);
            }
        }
    }

    private class GetInvestment extends AsyncTask<Integer,Void, ArrayList<Investment>>{

        @Override
        protected ArrayList<Investment> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor= db.query("investments",null,"user_id=?",
                        new String[] {String.valueOf(integers[0])}, null,null,"init_date DESC");

                if (null != cursor) {
                    if(cursor.moveToFirst()){
                        ArrayList<Investment> investments = new ArrayList<>();

                        for (int i=0; i< cursor.getCount();i++){
                            Investment investment = new Investment();
                            investment.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                            investment.setUser_id(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                            investment.setTransaction_id(cursor.getInt(cursor.getColumnIndexOrThrow("transaction_id")));
                            investment.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
                            investment.setFinish_date(cursor.getString(cursor.getColumnIndexOrThrow("finish_date")));
                            investment.setInit_date(cursor.getString(cursor.getColumnIndexOrThrow("init_date")));
                            investment.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                            investment.setMonthly_roi(cursor.getDouble(cursor.getColumnIndexOrThrow("monthly_roi")));
                            investments.add(investment);
                            cursor.moveToNext();
                        }
                        cursor.close();
                        db.close();
                        return investments;
                    }else{
                        db.close();
                        cursor.close();
                        return null;
                    }
                }else{
                    db.close();
                    return  null;
                }

            }catch (SQLException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Investment> investments) {
            if(investments !=null){
                adapter.setInvestments(investments);
            }else{
                adapter.setInvestments(new ArrayList<Investment>());
            }

            super.onPostExecute(investments);
        }
    }
    private void initViews() {
        Log.d(TAG, "initViews: started");
        recycleView = (RecyclerView) findViewById(R.id.investmentRecView);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
    }

    private void initBottomNavView() {
        Log.d(TAG, "initBottomNavView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_investments);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.menu_item_stats:
                        break;
                    case R.id.menu_item_transaction:
                        //TODO: complete this logic
                        break;
                    case R.id.menu_item_home:
                            Intent intent = new Intent(InvestmentActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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

}