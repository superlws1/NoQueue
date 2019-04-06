package com.example.noqueue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.noqueue.Firebase.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Class that allows service providers to setup their service
 */
public class ProviderSettings extends AppCompatActivity {

    Button loadBtn;
    Button saveBtn;
    Button backBtn;
    Button addItemBtn;
    Button deleteItemBtn;
    Button providerSetupBtn;

    EditText itemTxt;
    EditText priceTxt;

    ListView itemList;
    ListView priceList;

    ArrayList<String> listItem = new ArrayList<String>();
    ArrayList<Double> listPrice = new ArrayList<Double>();

    ArrayAdapter<String> adapterItem;
    ArrayAdapter<Double> adapterPrice;

    Service service;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadBtn = findViewById(R.id.loadBtn);
        saveBtn = findViewById(R.id.saveBtn);
        backBtn = findViewById(R.id.backBtn);
        addItemBtn = findViewById(R.id.addItemBtn);
        deleteItemBtn = findViewById(R.id.deleteItemBtn);
        providerSetupBtn = findViewById(R.id.providerSetupBtn);

        itemTxt = findViewById(R.id.itemTxt);
        priceTxt = findViewById(R.id.priceTxt);

        itemList = findViewById(R.id.itemList);
        priceList = findViewById(R.id.priceList);

        service = new Service();

        Bundle b = getIntent().getExtras();
        latitude = b.getDouble("latitude");
        longitude = b.getDouble("longitude");

//        Gets the stored configuration of the service in the database from the user profile
        FirebaseDatabase.getInstance()
            .getReference()
            .child("users")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .child("service")
            .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Service.class) != null) {
                    service = dataSnapshot.getValue(Service.class);
                } else
                    service = new Service();
            }

            @Override
            public void onCancelled (DatabaseError databaseError) {
            }
        });

        adapterItem = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItem);
        itemList.setAdapter(adapterItem);
        adapterPrice = new ArrayAdapter<Double>(this, android.R.layout.simple_list_item_1, listPrice);
        priceList.setAdapter(adapterPrice);

//        Loads the configuration stored in the database
        loadBtn.setOnClickListener(v -> {
                saveBtn.setVisibility(View.VISIBLE);
                addItemBtn.setVisibility(View.VISIBLE);
                deleteItemBtn.setVisibility(View.VISIBLE);
                itemTxt.setVisibility(View.VISIBLE);
                priceTxt.setVisibility(View.VISIBLE);
                itemList.setVisibility(View.VISIBLE);
                priceList.setVisibility(View.VISIBLE);

                listItem = (ArrayList<String>) service.getItem().clone();
                listPrice = (ArrayList<Double>) service.getPrice().clone();

                adapterItem = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItem);
                itemList.setAdapter(adapterItem);
                adapterPrice = new ArrayAdapter<Double>(this, android.R.layout.simple_list_item_1, listPrice);
                priceList.setAdapter(adapterPrice);
        });

        saveBtn.setOnClickListener(v -> {
            service.saveItems(listItem,listPrice);
        });

        backBtn.setOnClickListener(v -> {
            finish();
        });

//        Adds the items into the list of available items
        addItemBtn.setOnClickListener(v -> {
            listItem.add(itemTxt.getText().toString());
            double priceVal = Double.parseDouble(priceTxt.getText().toString());
            priceVal = (double)Math.round(priceVal * 100d) / 100d;
            listPrice.add(priceVal);

            adapterItem.notifyDataSetChanged();
            adapterPrice.notifyDataSetChanged();

            service.setItem(listItem);
            service.setPrice(listPrice);

            System.out.println(listItem);
        });

//        Deletes the last item
        deleteItemBtn.setOnClickListener(v -> {
            listItem.remove(listItem.size() - 1);
            listPrice.remove(listPrice.size() - 1);

            adapterItem.notifyDataSetChanged();
            adapterPrice.notifyDataSetChanged();

            service.setItem(listItem);
            service.setPrice(listPrice);
        });

//        Opens the service at current location
        providerSetupBtn.setOnClickListener(v -> {
            service.setLatitude(latitude);
            service.setLongitude(longitude);
            service.setUpService();

            setResult(5);
            finish();

        });
    }

}
