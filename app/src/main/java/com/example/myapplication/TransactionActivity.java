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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.myapplication.Adapters.TransactionAdapter;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Models.Transaction;
import com.example.myapplication.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class TransactionActivity extends AppCompatActivity {
    private static final String TAG = "TransactionActivity";

    private BottomNavigationView bottomNavigationView;
    private RadioGroup rgType;
    private Button btnSearch;
    private EditText edtTxtMin;
    private RecyclerView transactionRecView;
    private TextView txtNoTransaction;
    private TransactionAdapter adapter;
    private DatabaseHelper databaseHelper;
    private GetTransaction getTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        initView();
        initBottomNavView();

        adapter = new TransactionAdapter();
        transactionRecView.setAdapter(adapter);
        transactionRecView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);

        initSearch();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSearch();
            }
        });

        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                initSearch();
            }
        });
    }

    private class GetTransaction extends AsyncTask<Integer, Void, ArrayList<Transaction>> {

        private String type = "all";
        private double min = 0.0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.min = Double.valueOf(edtTxtMin.getText().toString());
            switch (rgType.getCheckedRadioButtonId()) {
                case R.id.rbInvestment:
                    type = "investment";
                    break;
                case R.id.rbLoan:
                    type = "loan";
                    break;
                case R.id.rbLoanPayment:
                    type = "loan_payment";
                    break;
                case R.id.rbProfit:
                    type = "profit";
                    break;
                case R.id.rbSend:
                    type = "send";
                    break;
                case R.id.rbShopping:
                    type = "shopping";
                    break;
                case R.id.rbReceive:
                    type = "receive";
                    break;
                default:
                    type = "all";
                    break;
            }
        }

        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {

            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor;
                if (type.equals("all")) {
//                    cursor = db.query("transaction", null, "user_id=?",
//                            new String[]{String.valueOf(integers[0])}, null, null, "date DESC");

                     cursor = db.query("transactions", null, "user_id=?", new String[]{String.valueOf(integers[0])}, null, null, "date DESC");

                } else {
                    cursor = db.query("transactions", null, "type=? AND user_id=?",
                            new String[]{type, String.valueOf(integers[0])}, null, null, "date DESC");
                }

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        ArrayList<Transaction> transactions = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            Transaction transaction = new Transaction();
                            transaction.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                            transaction.setUser_id(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                            transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                            transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                            transaction.setRecipient(cursor.getString(cursor.getColumnIndexOrThrow("recipient")));
                            transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                            transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));

                            double absoluteAmount = transaction.getAmount();

                            if (absoluteAmount < 0) {
                                absoluteAmount = -absoluteAmount;
                            }

                            if (absoluteAmount > this.min) {
                                transactions.add(transaction);
                            }

                            cursor.moveToNext();
                        }
                        cursor.close();
                        db.close();
                        return transactions;
                    } else {
                        cursor.close();
                        db.close();
                        return null;
                    }
                } else {
                    db.close();
                    return null;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Transaction> transactions) {
            super.onPostExecute(transactions);

            if (transactions != null) {
                txtNoTransaction.setVisibility(View.GONE);
                adapter.setTransactions(transactions);
            } else {
                txtNoTransaction.setVisibility(View.VISIBLE);
                adapter.setTransactions(new ArrayList<Transaction>());
            }
        }
    }

    private void initSearch() {
        Log.d(TAG, "initSearch: started");

        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();

        if (user != null){
            getTransaction = new GetTransaction();
            getTransaction.execute(user.get_id());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(getTransaction!=null){
            if(!getTransaction.isCancelled())
                getTransaction.cancel(true);
        }
    }

    private void initView() {
        Log.d(TAG, "initView: started");
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
        rgType = (RadioGroup) findViewById(R.id.rgType);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        edtTxtMin = (EditText) findViewById(R.id.edtTxtMin);
        txtNoTransaction = (TextView) findViewById(R.id.txtNoTransaction);
        transactionRecView = (RecyclerView) findViewById(R.id.transactionRecView);

    }

    private void initBottomNavView() {
        Log.d(TAG, "initBottomNavView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_transaction);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.menu_item_stats:
                        Intent statsIntent = new Intent(TransactionActivity.this, StatsActivity.class);
                        statsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(statsIntent);
                        break;
                    case R.id.menu_item_transaction:
                        break;
                    case R.id.menu_item_home:
                        Intent homeIntent = new Intent(TransactionActivity.this, MainActivity.class);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(homeIntent);
                        break;
                    case R.id.menu_item_loan:
                        Intent loanIntent = new Intent(TransactionActivity.this, LoanActivity.class);
                        loanIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loanIntent);
                        break;
                    case R.id.menu_item_investments:
                        Intent investmentIntent = new Intent(TransactionActivity.this, InvestmentActivity.class);
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