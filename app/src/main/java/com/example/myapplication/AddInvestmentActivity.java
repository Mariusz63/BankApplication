package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddInvestmentActivity extends AppCompatActivity {
    private static final String TAG = "AddInvestmentActivity";

    private EditText edtTxtName, edtTxtInitAmount, edtTxtROI, edtTxtInitDate, edtTxtFinishDate;
    private Button btnPickInitDate, btnPickFinishDate, btnAddInvestment;
    private TextView txtWarning;

    private Calendar initCalendar = Calendar.getInstance();
    private Calendar finishCalendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener initDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            initCalendar.set(Calendar.YEAR,year);
            initCalendar.set(Calendar.MONTH,month);
            initCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            edtTxtInitDate.setText(new SimpleDateFormat("yyyy-MM--dd").format(initCalendar.getTime()));
        }
    };

    private DatePickerDialog.OnDateSetListener finishDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            finishCalendar.set(Calendar.YEAR,year);
            finishCalendar.set(Calendar.MONTH,month);
            finishCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            edtTxtFinishDate.setText(new SimpleDateFormat("yyyy-MM--dd").format(finishCalendar.getTime()));
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_investment);

        initView();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListeners: started");

        btnPickInitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddInvestmentActivity.this, initDateSetListener, initCalendar.get(Calendar.YEAR), initCalendar.get(Calendar.MONTH), initCalendar.get(Calendar.DAY_OF_MONTH));
            }
        });

        btnPickFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddInvestmentActivity.this, finishDateSetListener, finishCalendar.get(Calendar.YEAR), finishCalendar.get(Calendar.MONTH), finishCalendar.get(Calendar.DAY_OF_MONTH));

            }
        });
    }

    private void initView() {
        Log.d(TAG, "initView: started");

        edtTxtName = (EditText) findViewById(R.id.edtTxtName);
        edtTxtInitAmount = (EditText) findViewById(R.id.edtTxtInitAmount);
        edtTxtROI = (EditText) findViewById(R.id.edtTxtMonthlyROI);
        edtTxtInitDate = (EditText) findViewById(R.id.edtTxtInitDate);
        edtTxtFinishDate = (EditText) findViewById(R.id.edtTxtFinishDate);
        btnPickInitDate = (Button) findViewById(R.id.btnPickInitDate);
        btnPickFinishDate = (Button) findViewById(R.id.btnPickFinishDate);
        btnAddInvestment = (Button) findViewById(R.id.btnAddInvestment);
        txtWarning = (TextView) findViewById(R.id.txtWarning);


    }
}