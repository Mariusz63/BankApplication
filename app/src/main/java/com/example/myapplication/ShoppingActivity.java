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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.Adapters.ItemsAdapter;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Dialogs.SelectItemDialog;
import com.example.myapplication.Models.Item;
import com.example.myapplication.Models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ShoppingActivity extends AppCompatActivity implements ItemsAdapter.GetItem {
    private static final String TAG = "ShoppingActivity";

    private Calendar calendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            edtTxtDate.setText(new SimpleDateFormat("yyyy-MM--dd").format(calendar.getTime()));
        }
    };

    private TextView txtWarning, txtItemName;
    private ImageView itemImg;
    private Button btnPickItem, btnPickDate, btnAdd;
    private EditText edtTxtDate, edtTxtDesc, edtTxtItemPrice, edtTxtStore;
    private RelativeLayout itemRelLayout;
    private Item selectedItem;
    private DatabaseHelper databaseHelper;
    private AddShopping addShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        initView();

        btnPickDate.setOnClickListener((view) ->{
            new DatePickerDialog(ShoppingActivity.this,dateSetListener,calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPickItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectItemDialog selectItemDialog = new SelectItemDialog();
                selectItemDialog.show(getSupportFragmentManager(),"select item dialog");
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAdd();
            }

        });
    }

    private void initView() {
        Log.d(TAG, "initView: started");

        txtWarning = (TextView) findViewById(R.id.txtWarning);
        txtItemName = (TextView) findViewById(R.id.txtItemName);
        itemImg = (ImageView) findViewById(R.id.itemImg);
        btnPickDate = (Button) findViewById(R.id.btnPickDate);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnPickItem = (Button) findViewById(R.id.btnPick);
        edtTxtDate = (EditText) findViewById(R.id.edtTxtDate);
        edtTxtDesc = (EditText) findViewById(R.id.edtTxtDesc);
        edtTxtItemPrice = (EditText) findViewById(R.id.edtTxtPrice);
        edtTxtStore = (EditText) findViewById(R.id.edtTxtStore);
        itemRelLayout = (RelativeLayout) findViewById(R.id.itemRelLayout);

    }

    private void initAdd() {
        Log.d(TAG, "initAdd: started");

        if(selectedItem!=null){
            if(!edtTxtItemPrice.getText().toString().equals("")){
                if(!edtTxtItemPrice.getText().toString().equals("")){
                    addShopping = new AddShopping();
                    addShopping.execute();
                }else{
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please add date");
                }
            }else{
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("Please add price");
            }
        }else{
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText("Please select an Item");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(addShopping!=null){
            if(!addShopping.isCancelled()){
                addShopping.cancel(true);
            }
        }
    }

    @Override
    public void OnGettingItemResult(Item item) {
        Log.d(TAG, "OnGettingItemResult: item "+item.toString());
        selectedItem = item;
        itemRelLayout.setVisibility(View.VISIBLE);
        Glide.with(this).asBitmap().load(item.getImage_url()).into(itemImg);
        txtItemName.setText(item.getName());
        edtTxtDesc.setText(item.getDescription());
    }

    private class AddShopping extends AsyncTask<Void, Void, Void>{

        private User loggedInUser;
        private String date;
        private double price;
        private String store;
        private String description;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Utils utils = new Utils(ShoppingActivity.this);
            loggedInUser = utils.isUserLoggedIn();
            this.date = edtTxtDate.getText().toString();
            this.price = -Double.valueOf(edtTxtItemPrice.getText().toString());
            this.store = edtTxtStore.getText().toString();
            this.description=edtTxtDesc.getText().toString();

            databaseHelper = new DatabaseHelper(ShoppingActivity.this);

        }

        @Override
        protected Void doInBackground(Void... voids) {

            try{
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                ContentValues transactionValue = new ContentValues();
                transactionValue.put("amount", price);
                transactionValue.put("description", description);
                transactionValue.put("user_id", loggedInUser.get_id());
                transactionValue.put("type", "shopping");
                transactionValue.put("date", date);
                transactionValue.put("recipient", store);
               long transaction_id= db.insert("transactions",null,transactionValue);

                ContentValues shoppingValues = new ContentValues();
                shoppingValues.put("item_id", selectedItem.get_id());
                shoppingValues.put("transaction_id", transaction_id);
                shoppingValues.put("user_id", loggedInUser.get_id());
                shoppingValues.put("description", description);
                shoppingValues.put("date", date);
                long shoppingValue_id = db.insert("shopping",null,shoppingValues);

                Log.d(TAG, "doInBackground: shopping id: "+shoppingValue_id);

                Cursor cursor = db.query("users",new String[] {"remained_amount"}, "_id=?",
                        new String[] {String.valueOf(loggedInUser.get_id())}, null,null,null);

               if(cursor!=null){
                   if(cursor.moveToFirst()){
                       double remainedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("remained_amount"));
                       ContentValues amountValues = new ContentValues();
                       amountValues.put("remained_amount", remainedAmount-price);
                       int affectedRows = db.update("users",amountValues,"_id=?", new String[] {String.valueOf(loggedInUser.get_id())});
                       Log.d(TAG, "doInBackground: affected rows: "+ affectedRows);
                   }

                   cursor.close();
               }
               db.close();

            }catch (SQLException e){
                e.printStackTrace();
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Toast.makeText(ShoppingActivity.this,selectedItem.getName() + " added successfully!",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(ShoppingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}