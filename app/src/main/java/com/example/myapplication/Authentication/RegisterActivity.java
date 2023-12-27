package com.example.myapplication.Authentication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.MainActivity;
import com.example.myapplication.Models.User;
import com.example.myapplication.R;
import com.example.myapplication.Utils;
import com.example.myapplication.WebsiteActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtTxtEmail, edtTxtPasword, edtTxtAddres, edtTxtName;
    private TextView txtWarning, txtLogin, txtLicence;
    private ImageView firstImage, secondImage, thirdImage, forthImage, fifthImage;
    private Button btnRegister;
    private DatabaseHelper databaseHelper;
    private String image_url;

    private DoesUserExist doesUserExist;
    private RegisterUser registerUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        databaseHelper = new DatabaseHelper(this);

        image_url = "first";
        handleImageUrl();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRegister();
            }
        });
        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        txtLicence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, WebsiteActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleImageUrl() {
        Log.d(TAG,"handleImageUrl: started");
        firstImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url="first";
            }
        });

        secondImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url="first";
            }
        });

        thirdImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url="first";
            }
        });

        forthImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url="first";
            }
        });

        fifthImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url="first";
            }
        });
    }

    private void initRegister(){
        Log.d(TAG,"initRegister: started");
        String email = edtTxtEmail.getText().toString();
        String password = edtTxtPasword.getText().toString();

        if(email.equals("") || password.equals("")){
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText("Please enter the password and email ");
        }else{
            txtWarning.setVisibility(View.GONE);

            doesUserExist = new DoesUserExist();
            doesUserExist.execute(email);
        }
    }

    private class DoesUserExist extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {
           try{
               SQLiteDatabase db = databaseHelper.getReadableDatabase();
               Cursor cursor = db.query("users", new String[]{"_id","email"},"email=?",
                       new String[] {strings[0]},null,null,null);
               if(null!=cursor){
                   if(cursor.moveToFirst()){
                    if(cursor.getString(cursor.getColumnIndexOrThrow("email")).equals(strings[0])){
                        cursor.close();
                        db.close();
                        return true;
                    }else{
                        cursor.close();
                        db.close();
                        return false;
                    }
                   }else{
                       cursor.close();
                       db.close();
                       return false;
                   }
               }else{
                   db.close();
                   return true;
               }
           }catch (SQLException e){
               e.printStackTrace();

               return true;
           }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean){
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("There is user with this email, please try another email");
            }else{
                txtWarning.setVisibility(View.GONE);
                registerUser = new RegisterUser();
                registerUser.execute();
            }
        }
    }

    private class RegisterUser extends AsyncTask<Void, Void, User>{
        private String email;
        private String password;
        private String address;
        private String first_name;
        private String last_name;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            String email = edtTxtEmail.getText().toString();
            String password = edtTxtPasword.getText().toString();
            String address = edtTxtAddres.getText().toString();
            String name = edtTxtName.getText().toString();

            this.email = email;
            this.password = password;
            this.address = address;

            String[] names = name.split(" ");
            if(names.length >=1){
                this.first_name = names[0];
                this.last_name = "";

                for(int i=1;i<names.length;i++){
                    if (i > 1) {
                        last_name += " "+names[i];
                    }else{
                        last_name += names[i];
                    }
                }
            }else{
                this.first_name = "";
                this.last_name = "";
            }
        }

        @Override
        protected User doInBackground(Void... voids) {
            try{
                SQLiteDatabase db = databaseHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put("email",this.email);
                values.put("password",this.password);
                values.put("address",this.address);
                values.put("first_name",this.first_name);
                values.put("last_name",this.last_name);
                values.put("remained_amount",0.0);
                values.put("image_url",image_url);

                long userId = db.insert("users",null,values);
                Log.d(TAG,"doInBackground: userId");

                Cursor cursor = db.query("users",null,"_id=?",
                        new String[] {String.valueOf(userId)},null,null,null);

                if(null!=cursor){
                    if(cursor.moveToFirst()){
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
                    }else{
                        cursor.close();
                        db.close();
                        return  null;
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
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            if(user != null){
                 Toast.makeText(RegisterActivity.this, "User " + user.getEmail() + " registered successfully!",Toast.LENGTH_SHORT).show();
                Utils utils = new Utils(RegisterActivity.this);
                utils.addUserToSharedPreferences(user);
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else{
                Toast.makeText(RegisterActivity.this, "Wasn't able to register, please try again later",Toast.LENGTH_SHORT).show();

            }
        }
    }
    private void initViews() {
        Log.d(TAG,"initViews: called");

        edtTxtEmail = (EditText) findViewById(R.id.editTxtEmail);
        edtTxtPasword= (EditText) findViewById(R.id.editTxtPassword);
        edtTxtAddres = (EditText) findViewById(R.id.editTxtAddress);
        edtTxtName = (EditText) findViewById(R.id.editTxtName);

        txtWarning = (TextView) findViewById(R.id.txtWarning);
        txtLogin = (TextView) findViewById(R.id.txtLogin);
        txtLicence = (TextView) findViewById(R.id.txtLicense);

        firstImage = (ImageView) findViewById(R.id.firstImage);
        secondImage = (ImageView) findViewById(R.id.secondImage);
        thirdImage = (ImageView) findViewById(R.id.thirdImage);
        forthImage = (ImageView) findViewById(R.id.forthImage);
        fifthImage = (ImageView) findViewById(R.id.fifthImage);

        btnRegister = (Button) findViewById(R.id.btnRegister);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(null!= doesUserExist){
            if(!doesUserExist.isCancelled()){
                doesUserExist.cancel(true);
            }
        }

        if(null!= registerUser){
            if(!registerUser.isCancelled()){
                registerUser.cancel(true);
            }
        }
    }
}