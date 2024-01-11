package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//TODO: repair this class
public class ConversionActivity extends AppCompatActivity {
    private static final String TAG = "ConversionActivity";

    String baseCurrency = "USD";
    String convertedToCurrency = "EUR";
    float conversionRate = 0f;

    private EditText et_firstConversion;
    private EditText et_secondConversion;
    private Spinner spinner_firstConversion;
    private Spinner spinner_secondConversion;
    private BottomNavigationView bottomNavigationView;

    private DatabaseHelper databaseHelper;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);

        initView();
        spinnerSetup();
        textChangedStuff();
        initBottomNavView();

        utils = new Utils(this);
        databaseHelper = new DatabaseHelper(this);
        
    }

    private void initView() {
        CardView cardView = findViewById(R.id.cardViewConversion);
        et_firstConversion = findViewById(R.id.et_firstConversion);
        et_secondConversion = findViewById(R.id.et_secondConversion);
        spinner_firstConversion = findViewById(R.id.spinner_firstConversion);
        spinner_secondConversion = findViewById(R.id.spinner_secondConversion);
    }

    private void textChangedStuff() {
        et_firstConversion.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    getApiResult();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Type a value", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "Before Text Changed");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "OnTextChanged");
            }
        });
    }

    private void getApiResult() {
        if (et_firstConversion != null && et_firstConversion.getText().length() > 0 && !et_firstConversion.getText().toString().trim().isEmpty()) {
            String API = "https://api.ratesapi.io/api/latest?base=" + baseCurrency + "&symbols=" + convertedToCurrency;

            if (baseCurrency.equals(convertedToCurrency)) {
                Toast.makeText(getApplicationContext(), "Please pick a currency to convert", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(API);
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }

                                String apiResult = response.toString();
                                JSONObject jsonObject = new JSONObject(apiResult);
                                conversionRate = Float.parseFloat(jsonObject.getJSONObject("rates").getString(convertedToCurrency));
                                Log.d(TAG, String.valueOf(conversionRate));
                                Log.d(TAG, apiResult);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String text = String.valueOf(Float.parseFloat(et_firstConversion.getText().toString()) * conversionRate);
                                        et_secondConversion.setText(text);

                                        // Save conversion details to the database
                                        saveConversionToDatabase(Float.parseFloat(et_firstConversion.getText().toString()), Float.parseFloat(text));
                                    }
                                });

                            } catch (Exception e) {
                                Log.e(TAG, String.valueOf(e));
                            } finally {
                                urlConnection.disconnect();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, String.valueOf(e));
                        }
                    }
                }).start();
            }
        }
    }

    private void saveConversionToDatabase(float originalAmount, float resultAmount) {
        User user = getUserFromDatabase(); // Retrieve user details from the database

        if (user != null) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("user_id", user.get_id());
            values.put("base_currency", baseCurrency);
            values.put("converted_currency", convertedToCurrency);
            values.put("amount", originalAmount);
            values.put("conversion_rate", conversionRate);
            values.put("result", resultAmount);
            values.put("date", getCurrentDate());

            long newRowId = db.insert("conversions", null, values);

            Log.d(TAG, "Conversion saved to database. Row ID: " + newRowId);

            db.close();
        }
    }

    private User getUserFromDatabase() {
        // Implement code to retrieve the logged-in user from the users table
        // Return the User object or null if user not found
        User user = utils.isUserLoggedIn();
        return user;
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void spinnerSetup() {
        Spinner spinner = findViewById(R.id.spinner_firstConversion);
        Spinner spinner2 = findViewById(R.id.spinner_secondConversion);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.currencies,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this,
                R.array.currencies2,
                android.R.layout.simple_spinner_item
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not implemented yet
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                baseCurrency = parent.getItemAtPosition(position).toString();
                getApiResult();
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not implemented yet
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                convertedToCurrency = parent.getItemAtPosition(position).toString();
                getApiResult();
            }
        });
    }

    private void initBottomNavView() {
        Log.d(TAG, "initBottomNavView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.menu_item_stats:
                        Intent statsIntent = new Intent(ConversionActivity.this, StatsActivity.class);
                        statsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(statsIntent);
                        break;
                    case R.id.menu_item_transaction:
                        Intent transactionIntent = new Intent(ConversionActivity.this, TransactionActivity.class);
                        transactionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(transactionIntent);
                        break;
                    case R.id.menu_item_home:
                        Intent homeIntent = new Intent(ConversionActivity.this, MainActivity.class);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(homeIntent);
                        break;
                    case R.id.menu_item_loan:
                        Intent loanIntent = new Intent(ConversionActivity.this, LoanActivity.class);
                        loanIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loanIntent);
                        break;
                    case R.id.menu_item_investments:
                        Intent investmentIntent = new Intent(ConversionActivity.this, InvestmentActivity.class);
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