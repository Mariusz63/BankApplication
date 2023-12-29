package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddLoanActivity extends AppCompatActivity {
    private static final String TAG = "AddLoanActivity";

    private TextView txtWarning;
    private EditText edtTxtName, edtTxtInitAmount, edtTxtROI, edtTxtInitDate, edtTxtFinishDate, edtTxtMonthlyPayment;
    private Button btnPickInitDate, btnPickFinishDate, btnAddLoan;

    private Calendar initCalendar = Calendar.getInstance();
    private Calendar finishCalendar = Calendar.getInstance();
    private Utils utils;
    private AddTransaction addTransaction;
    private AddLoan addLoan;
    private DatabaseHelper databaseHelper;

    private DatePickerDialog.OnDateSetListener initDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            initCalendar.set(Calendar.YEAR,year);
            initCalendar.set(Calendar.MONTH,month);
            initCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            edtTxtInitDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(initCalendar.getTime()));
        }
    };

    private DatePickerDialog.OnDateSetListener finishDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            finishCalendar.set(Calendar.YEAR,year);
            finishCalendar.set(Calendar.MONTH,month);
            finishCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            edtTxtFinishDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(finishCalendar.getTime()));
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_loan);

        initView();

        databaseHelper = new DatabaseHelper(this);
        utils= new Utils(this);

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListeners: started");

        btnPickInitDate.setOnClickListener((view) ->{

            new DatePickerDialog(AddLoanActivity.this,
                    initDateSetListener, initCalendar.get(Calendar.YEAR), initCalendar.get(Calendar.MONTH), initCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPickFinishDate.setOnClickListener((view) ->{
            new DatePickerDialog(AddLoanActivity.this,
                    finishDateSetListener, finishCalendar.get(Calendar.YEAR), finishCalendar.get(Calendar.MONTH), finishCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnAddLoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateData()){
                    txtWarning.setVisibility(View.GONE);
                    initAdding();
                }else{
                    txtWarning.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initAdding(){
        Log.d(TAG, "initAdding: started");
        User user = utils.isUserLoggedIn();
        if(user!=null){
            addTransaction = new AddTransaction();
            addTransaction.execute(user.get_id());
        }
    }

    private class AddTransaction extends AsyncTask<Integer, Void, Integer>{
        private String date, name;
        private double amount;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.date = edtTxtInitDate.getText().toString();
            this.name = edtTxtName.getText().toString();
            this.amount = -Double.valueOf( edtTxtInitAmount.getText().toString());
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();

                ContentValues values = new ContentValues();
                values.put("amount",amount);
                values.put("recipient", name);
                values.put("date", date);
                values.put("description", "Received amount for "+name);
                values.put("user_id",integers[0]);
                values.put("type","loan");

                long id = db.insert("transactions", null, values);
                return (int) id;

            }catch (SQLException e){
                e.printStackTrace();
                return  null;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer!=null){

                addLoan = new AddLoan();
                addLoan.execute(integer);
            }
        }
    }

    private class AddLoan extends AsyncTask<Integer, Void, Integer>{
        private int userId;
        private String initDate, finishDate,name;
        private double monthlyROI, initAmount , monthlyPayment;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.initAmount = Double.valueOf(edtTxtInitAmount.getText().toString());
            this.monthlyROI = Double.valueOf((edtTxtROI.getText().toString()));
            this.monthlyPayment = Double.valueOf((edtTxtMonthlyPayment.getText().toString()));
            this.name = edtTxtName.getText().toString();
            this.initDate = edtTxtInitDate.getText().toString();
            this.finishDate = edtTxtFinishDate.getText().toString();
            User user = utils.isUserLoggedIn();

            if(user!= null){
                this.userId = user.get_id();
            }else{
                this.userId = -1;
            }
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            if(userId!=-1){
                try{
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("name",name);
                    values.put("init_date",initDate);
                    values.put("finish_date",finishDate);
                    values.put("init_amount",initAmount);
                    values.put("remained_amount",initAmount);
                    values.put("monthly_payment",monthlyROI);
                    values.put("monthly_roi",monthlyROI);
                    values.put("user_id",userId);
                    values.put("transaction_id",integers[0]);

                    long loanId = db.insert("loans",null,values); //throw -1 if an error occurred

                    if(loanId!=-1){
                        Cursor cursor = db.query("users", new String[] {"remained_amount"}, "_id=?",
                                new String[] {String.valueOf(userId)},null,null,null);

                        if(cursor!=null){
                            if(cursor.moveToFirst()){
                                double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("remained_amount"));
                                ContentValues newValues = new ContentValues();
                                newValues.put("remained_amount", currentRemainedAmount + initAmount);
                                int affectedRows = db.update("remained_amount", newValues, "_id=?", new String[] {String.valueOf(userId)});
                                Log.d(TAG, "doInBackground: updateRows: " + affectedRows);
                                cursor.close();
                                return (int) loanId;
                            }else {
                                cursor.close();
                                db.close();
                                return null;
                            }
                        }else{
                            cursor.close();
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
            }else{
                return  null;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if(integer!=null){
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                try {
                    Date initDate = sdf.parse(this.initDate);
                    calendar.setTime(initDate);
                    int initMonths = calendar.get(Calendar.YEAR)*12+ calendar.get(Calendar.MONTH);

                    Date finishDate = sdf.parse(this.finishDate);
                    calendar.setTime(finishDate);
                    int finishMonths =calendar.get(Calendar.YEAR)*12+ calendar.get(Calendar.MONTH);

                    int difference = finishMonths-initMonths;
                    int days = 0;

                    for(int i=0;i<difference;i++) {
                        days += 30;

                        Data data = new Data.Builder()
                                .putInt("loan_id", integer)
                                .putInt("user_id", userId)
                                .putDouble("monthly_payment", monthlyPayment)
                                .putString("name",name)
                                .build();

                        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).build();

                        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LoanWorker.class)
                                .setInputData(data)
                                .setConstraints(constraints)
                                .setInitialDelay(days, TimeUnit.DAYS)
                                .addTag("loan_payment")
                                .build();

                        WorkManager.getInstance(AddLoanActivity.this).enqueue(request);

                        Intent intent = new Intent(AddLoanActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }


            }
        }
    }

    private boolean validateData(){
        Log.d(TAG, "validateData: started");
        if(edtTxtName.getText().toString().equals("")){
            return  false;
        }

        if(edtTxtInitDate.getText().toString().equals("")){
            return  false;
        }

        if(edtTxtFinishDate.getText().toString().equals("")){
            return  false;
        }

        if(edtTxtInitAmount.getText().toString().equals("")){
            return  false;
        }

        if(edtTxtROI.getText().toString().equals("")){
            return  false;
        }

        if(edtTxtMonthlyPayment.getText().toString().equals("")){
            return  false;
        }

        return true;
    }

    protected void onDestroy() {
        super.onDestroy();

        if(addTransaction!=null){
            if(!addTransaction.isCancelled()){
                addTransaction.cancel(true);
            }
        }

        if(addLoan!=null){
            if(!addLoan.isCancelled()){
                addLoan.cancel(true);
            }
        }

    }

    private void initView() {
        Log.d(TAG, "initView: started");

        edtTxtName = (EditText) findViewById(R.id.edtTxtName);
        edtTxtInitAmount = (EditText) findViewById(R.id.edtTxtInitAmount);
        edtTxtMonthlyPayment = (EditText) findViewById(R.id.edtTxtMonthlyPayment);
        edtTxtROI = (EditText) findViewById(R.id.edtTxtMonthlyROI);
        edtTxtInitDate = (EditText) findViewById(R.id.edtTxtInitDate);
        edtTxtFinishDate = (EditText) findViewById(R.id.edtTxtFinishDate);
        btnPickInitDate = (Button) findViewById(R.id.btnPickInitDate);
        btnPickFinishDate = (Button) findViewById(R.id.btnPickFinishDateLoan);
        btnAddLoan = (Button) findViewById(R.id.btnAddLoan);
        txtWarning = (TextView) findViewById(R.id.txtWarning);
    }
}