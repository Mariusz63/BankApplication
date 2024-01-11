package com.example.myapplication.Authentication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.MainActivity;
import com.example.myapplication.Models.User;
import com.example.myapplication.R;
import com.example.myapplication.Utils;
import com.example.myapplication.WebsiteActivity;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText edtTxtEmail, edtTxtPassword;
    private TextView txtWarning, txtLicense, txtRegister;
    private Button btnLogin;

    private DatabaseHelper databaseHelper;
    private LoginUser loginUser;
    private DoesEmailExist doesEmailExist;

    private Location lastLocation;

    private FusedLocationProviderClient fusedLocationClient;
    public static final int REQUEST_LOCATION_PERMISSION = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        txtLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, WebsiteActivity.class);
                startActivity(intent);
            }
        });

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
                executeGeocoding();
                initLogin();
            }
        });
    }

    private void initLogin() {
        Log.d(TAG, "initLogin: started");
        if (!edtTxtEmail.getText().toString().equals("")) {
            if (!edtTxtEmail.getText().toString().equals("")) {
                txtWarning.setVisibility(View.GONE);

                doesEmailExist = new DoesEmailExist();
                doesEmailExist.execute(edtTxtEmail.getText().toString());
            } else {
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("Please enter your Password");
            }
        } else {
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText("Please enter your email address");
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews: called");
        edtTxtEmail = (EditText) findViewById(R.id.edtTxtEmail);
        edtTxtPassword = (EditText) findViewById(R.id.edtTxtPassword);
        txtWarning = (TextView) findViewById(R.id.txtWarning);
        txtLicense = (TextView) findViewById(R.id.txtLicense);
        txtRegister = (TextView) findViewById(R.id.txtRegister);
        btnLogin = (Button) findViewById(R.id.btnLogin);

    }

    private class DoesEmailExist extends AsyncTask<String, Void, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            databaseHelper = new DatabaseHelper(LoginActivity.this);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("users", new String[]{"email"}, "email=?",
                        new String[]{strings[0]}, null, null, null);

                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        cursor.close();
                        db.close();
                        return true;
                    } else {
                        cursor.close();
                        db.close();
                        return false;
                    }
                } else {
                    db.close();
                    return false;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                loginUser = new LoginUser(LoginActivity.this);
                loginUser.execute();

            } else {
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("There is no such user with this email address");
            }
        }
    }

    private class LoginUser extends AsyncTask<Void, Void, User> {
        private String email;
        private String password;
        private Context context;

        public LoginUser(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.email = edtTxtEmail.getText().toString();
            this.password = edtTxtPassword.getText().toString();
        }

        @Override
        protected User doInBackground(Void... voids) {
            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("users", null, "email=? AND password=?",
                        new String[]{email, password}, null, null, null);

                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        User user = new User();
                        user.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                        user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                        user.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                        user.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                        user.setImage_url(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
                        user.setRemained_amount(cursor.getInt(cursor.getColumnIndexOrThrow("remained_amount")));

                        cursor.close();
                        db.close();
                        return user;

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
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            if (null != user) {
                Utils utils = new Utils(LoginActivity.this);
                utils.addUserToSharedPreferences(user);

                // Add location tracking here
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        double login_time = location.getTime();

                        // Save the location to the user's login history
                        saveLocationToLoginHistory(user.get_id(),login_time, latitude, longitude);
                    }
                }


                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("Your password is wrong");
            }
        }

        private Integer saveLocationToLoginHistory(int userId, double login_time, double latitude, double longitude) {
            try {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                values.put("login_time", login_time);
                values.put("login_location", "Latitude: " + latitude + ", Longitude: " + longitude);
                long id = db.insert("user_login_history", null, values);
                return (int) id;

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    private void getLocation(){
        Log.d(TAG, "getLocation: started");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else
        {
            Log.d(TAG, "getLocation: permissions granted");
        }

    }

    private String locationGeocoding (Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        String resultMessage = "";
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            resultMessage = context
                    .getString(R.string.service_not_available);
            Log.e(TAG, resultMessage, ioException);
        }

        if (addresses == null || addresses.isEmpty()) {
            if (resultMessage.isEmpty()) {
                resultMessage = context
                        .getString(R.string.no_address_found);
                Log.e(TAG, resultMessage);
            }
        } else {
            Address address = addresses.get(0);
            List<String> addressParts = new ArrayList<>();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressParts.add(address.getAddressLine(i));
            }
            resultMessage = TextUtils.join("\n", addressParts);
        }

        return resultMessage;
    }

    private void executeGeocoding() {
        if (lastLocation != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> returnedAddress = executor.submit(() -> locationGeocoding(getApplicationContext(), lastLocation));
            try {
                String geocodingLocationResult = returnedAddress.get();

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != doesEmailExist) {
            if (!doesEmailExist.isCancelled()) {
                doesEmailExist.cancel(true);
            }
        }

        if (null != loginUser) {
            if (!loginUser.isCancelled()) {
                loginUser.cancel(true);
            }
        }

    }
}