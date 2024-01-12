package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConversionActivity extends AppCompatActivity {
    private static final String TAG = "ConversionActivity";

    private String baseCurrency = "EUR";
    private String convertedToCurrency = "USD";
    private float conversionRate = 0f;
    private EditText et_firstConversion;
    private EditText et_secondConversion;
    private String APIkey = "ae63cce981362783e99a5ed7206cf14f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);

        et_firstConversion = findViewById(R.id.et_firstConversion);
        et_secondConversion = findViewById(R.id.et_secondConversion);

        spinnerSetup();
        textChangedStuff();
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
                Log.d("ConversionActivity", "Before Text Changed");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("ConversionActivity", "OnTextChanged");
            }
        });
    }

    private void getApiResult() {
        if (et_firstConversion != null && et_firstConversion.getText().length() > 0 && !et_firstConversion.getText().toString().trim().isEmpty()) {
            String API = "https://api.exchangeratesapi.io/v1/latest?access_key="+APIkey+"&base=" + baseCurrency + "&symbols=" + convertedToCurrency;

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

/*
    private void getApiResult() {
        Log.d(TAG, "getApiResult: started");
        if (et_firstConversion != null && et_firstConversion.getText().length() > 0
                && !et_firstConversion.getText().toString().trim().isEmpty()) {

            String firstCurrencyAPI = "http://api.exchangeratesapi.io/v1/latest?access_key=" + APIkey + "&symbols=" + baseCurrency + "&format=1";
            String secondCurrencyAPI = "http://api.exchangeratesapi.io/v1/latest?access_key=" + APIkey + "&symbols=" + convertedToCurrency + "&format=1";

            if (baseCurrency.equals(convertedToCurrency)) {
                Toast.makeText(getApplicationContext(), "Please pick a currency to convert", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String firstCurrencyResult = new URL(firstCurrencyAPI).openStream().toString();
                            JSONObject firstCurrencyJsonObject = new JSONObject(firstCurrencyResult);
                            float firstCurrencyConversionRate = Float.parseFloat(firstCurrencyJsonObject.getJSONObject("rates")
                                    .getString(convertedToCurrency));

                            Log.d("getApiResult", "First Currency: " + String.valueOf(firstCurrencyConversionRate));
                            Log.d("getApiResult", firstCurrencyResult);

                            // Now download data for the second currency
                            String secondCurrencyResult = new URL(secondCurrencyAPI).openStream().toString();
                            JSONObject secondCurrencyJsonObject = new JSONObject(secondCurrencyResult);
                            float secondCurrencyConversionRate = Float.parseFloat(secondCurrencyJsonObject.getJSONObject("rates")
                                    .getString(baseCurrency));

                            Log.d("getApiResult", "Second Currency: " + String.valueOf(secondCurrencyConversionRate));
                            Log.d("getApiResult", secondCurrencyResult);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Process the data as needed
                                }
                            });

                        } catch (Exception e) {
                            Log.e("getApiResult", String.valueOf(e));
                        }
                    }
                }).start();
            }
        }
    }
*/


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
}
