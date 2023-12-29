package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TransferActivity extends AppCompatActivity {
    private static final String TAG = "TransferActivity";

    private EditText edtTxtAmount, edtTxtDescription, edtTxtRecipient, edtTxtDate;
    private TextView txtWarning;
    private Button btnPickDate, btnAdd;
    private RadioGroup rgType;
    private Calendar calendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            edtTxtDate.setText(new SimpleDateFormat("yyyy-MM--dd").format(calendar.getTime()));
        }
    };

    private AddTransaction addTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        initViews();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListeners: started");

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(TransferActivity.this,
                        dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateData()){
                    txtWarning.setVisibility(View.GONE);
                    initAdding();
                }else{
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please fill all information");
                }
            }
        });
    }

    private void initAdding(){
        Log.d(TAG, "initAdding: started");
        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();

        if(user!=null){
            //TODO: exec

            addTransaction = new AddTransaction();
            addTransaction.execute(user.get_id());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(addTransaction!=null){
            if(!addTransaction.isCancelled()){
                addTransaction.cancel(true);
            }
        }
    }

    private class AddTransaction extends AsyncTask<Integer,Void, Void>{

        private double amount;
        private String recipient, date, description ,type;
        private DatabaseHelper databaseHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.amount = Double.valueOf(edtTxtAmount.getText().toString());
            this.date = edtTxtDate.getText().toString();
            this.recipient = edtTxtRecipient.getText().toString();
            this.description = edtTxtDescription.getText().toString();

            int rbId = rgType.getCheckedRadioButtonId();
            switch (rbId){
                case R.id.btnReceive:
                    type = "receive";
                    break;
                case R.id.btnSend:
                    type= "send";
                    amount= -amount;
                    break;
                default:
                    break;
            }

            databaseHelper = new DatabaseHelper(TransferActivity.this);
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            try{
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("amount",this.amount);
                values.put("recipient",recipient);
                values.put("date",date);
                values.put("type",type);
                values.put("description",description);
                values.put("user_id",integers[0]);

                Log.d(TAG, "doInBackground: amount: "+ this.amount);

                long id = db.insert("transactions", null, values);
                Log.d(TAG, "doInBackground: new Transaction id"+ id);
                if(id!=-1){
                    Cursor cursor = db.query("users", new String[] {"remained_amount"}, "id=?", new String[] {String.valueOf(integers[0])}, null,null,null);

                    if(cursor!=null){
                        if(cursor.moveToFirst()){
                            double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("remained_amount"));
                            cursor.close();
                            ContentValues newValues = new ContentValues();
                            newValues.put("remained_amount", currentRemainedAmount + amount);
                            int affectedRows = db.update("users", newValues, "_id=?", new String[] {String.valueOf(integers[0])});
                            Log.d(TAG, "doInBackground: updateRows: " + affectedRows);
                            db.close();
                        }else {
                            cursor.close();
                            db.close();
                           // return null;
                        }
                    }else{
                        db.close();
                    }
                }else{
                    db.close();
                }


            }catch (SQLException e)
            {
                e.printStackTrace();
               // return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            Intent intent = new Intent(TransferActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
    private boolean validateData(){
        Log.d(TAG, "validateData: started");
        if(edtTxtAmount.getText().toString().equals("")){
            return false;
        }
        if(edtTxtDate.getText().toString().equals("")){
            return false;
        }

        if(edtTxtRecipient.getText().toString().equals("")){
            return false;
        }
        return true;
    }

    private void initViews() {
        Log.d(TAG, "initViews: started");
        edtTxtAmount = (EditText) findViewById(R.id.edtTxtAmount);
        edtTxtDescription = (EditText) findViewById(R.id.edtTxtDescription);
        edtTxtRecipient = (EditText) findViewById(R.id.edtTxtRecipient);
        edtTxtDate = (EditText) findViewById(R.id.edtTxtDate);
        txtWarning = (TextView) findViewById(R.id.txtWarning);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnPickDate = (Button) findViewById(R.id.btnPickDate);
        rgType = (RadioGroup) findViewById(R.id.rgType);
    }
}