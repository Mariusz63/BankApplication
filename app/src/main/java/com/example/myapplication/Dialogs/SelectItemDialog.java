package com.example.myapplication.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapters.ItemsAdapter;
import com.example.myapplication.Database.DatabaseHelper;
import com.example.myapplication.Models.Item;
import com.example.myapplication.R;

import java.util.ArrayList;

public class SelectItemDialog extends DialogFragment implements ItemsAdapter.GetItem {
    private static final String TAG = "SelectItemDialog";

    private EditText edtTxtItemName;
    private RecyclerView itemRecView;

    private ItemsAdapter.GetItem getItem;
    private ItemsAdapter itemsAdapter;
    private DatabaseHelper databaseHelper;
    private GetAllItems getAllItems;
    private SearchForItems searchForItems;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_item,null);

        edtTxtItemName = (EditText) view.findViewById(R.id.edtTxtItemName);
        itemRecView = (RecyclerView) view.findViewById(R.id.itemsRelLayout);

        itemsAdapter = new ItemsAdapter(getActivity(),this);
        itemRecView.setAdapter(itemsAdapter);
        itemRecView.setLayoutManager(new LinearLayoutManager(getActivity()));

        databaseHelper = new DatabaseHelper(getActivity());

        edtTxtItemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchForItems = new SearchForItems();
                searchForItems.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getAllItems = new GetAllItems();
        getAllItems.execute();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view).setTitle("Select an item");

        return builder.create();
    }

    @Override
    public void OnGettingItemResult(Item item) {
        Log.d(TAG, "OnGettingItemResult: item: "+item.toString());
        try {
            getItem=(ItemsAdapter.GetItem) getActivity();
            getItem.OnGettingItemResult(item);
            dismiss();
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    private class GetAllItems extends AsyncTask<Void, Void, ArrayList<Item>> {

        @Override
        protected ArrayList<Item> doInBackground(Void... voids) {
            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("items",null,null,null,null,null,null);

                if(cursor!=null){
                    if(cursor.moveToFirst()){
                        ArrayList<Item> items = new ArrayList<>();
                        for(int i=0; i<cursor.getCount();i++){
                            Item item = new Item();
                            item.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                            item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                            item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                            item.setImage_url(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                            items.add(item);
                            cursor.moveToNext();
                        }
                        cursor.close();
                        db.close();
                        return items;
                    }else{
                        cursor.close();
                        db.close();
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
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            if(items!=null){
                itemsAdapter.setItems(items);
            }else{
                itemsAdapter.setItems(new ArrayList<>());
            }

        }
    }

    private class SearchForItems extends AsyncTask<String, Void,ArrayList<Item>>{

        @Override
        protected ArrayList<Item> doInBackground(String... strings) {

            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("items", null, "name LIKE ?", new String[] {strings[0]}, null, null, null);

                if(cursor!=null){
                    ArrayList<Item> items = new ArrayList<>();
                    for(int i=0; i<cursor.getCount();i++){
                        Item item = new Item();
                        item.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
                        item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                        item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                        item.setImage_url(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                        items.add(item);
                        cursor.moveToNext();
                    }
                    cursor.close();
                    db.close();
                    return items;
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
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            if(items!=null){
                itemsAdapter.setItems(items);
            }else{
                itemsAdapter.setItems(new ArrayList<>());
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(getAllItems!=null){
            if(!getAllItems.isCancelled()){
                getAllItems.cancel(true);
            }
        }

        if(searchForItems!=null){
            if(!searchForItems.isCancelled()){
                searchForItems.cancel(true);
            }
        }
    }
}
