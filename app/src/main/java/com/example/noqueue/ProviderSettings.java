package com.example.noqueue;

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

    Button cancelBtn;
    Button loadBtn;
    Button saveBtn;
    Button addItemBtn;
    Button deleteItemBtn;
    Button clearBtn;
    Button providerSetupBtn;

    TextView messageView;
    EditText itemTxt;
    EditText priceTxt;

    ListView itemList;
    ListView priceList;

    Spinner serviceDropdown;

    ArrayList<String> listItem = new ArrayList<String>();
    ArrayList<Double> listPrice = new ArrayList<Double>();

    ArrayAdapter<String> adapterItem;
    ArrayAdapter<Double> adapterPrice;

    Service service;

    double latitude;
    double longitude;
    int type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cancelBtn = findViewById(R.id.cancelBtn);
        loadBtn = findViewById(R.id.loadBtn);
        saveBtn = findViewById(R.id.saveBtn);
        addItemBtn = findViewById(R.id.addItemBtn);
        deleteItemBtn = findViewById(R.id.deleteItemBtn);
        clearBtn = findViewById(R.id.clearBtn);
        providerSetupBtn = findViewById(R.id.providerSetupBtn);

        messageView = findViewById(R.id.messageView);
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

//        Creates the dropdown for service provider to setup their service (Order/Assistance)
        serviceDropdown = findViewById(R.id.serviceDropdown);
        ArrayAdapter<CharSequence> serviceList =
                ArrayAdapter.createFromResource(this, R.array.service_type,android.R.layout.simple_spinner_item);

        serviceList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceDropdown.setAdapter(serviceList);
        serviceDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        type = 0;
                        saveBtn.setVisibility(View.INVISIBLE);
                        addItemBtn.setVisibility(View.INVISIBLE);
                        deleteItemBtn.setVisibility(View.INVISIBLE);
                        clearBtn.setVisibility(View.INVISIBLE);
                        itemTxt.setVisibility(View.INVISIBLE);
                        priceTxt.setVisibility(View.INVISIBLE);
                        itemList.setVisibility(View.INVISIBLE);
                        priceList.setVisibility(View.INVISIBLE);

                        listItem.clear();
                        listPrice.clear();
                        adapterItem = new ArrayAdapter<String>(ProviderSettings.this, android.R.layout.simple_list_item_1, listItem);
                        itemList.setAdapter(adapterItem);
                        adapterPrice = new ArrayAdapter<Double>(ProviderSettings.this, android.R.layout.simple_list_item_1, listPrice);
                        priceList.setAdapter(adapterPrice);
                        itemTxt.getText().clear();
                        priceTxt.getText().clear();

                        break;

                    case 1:
                        type = 1;
                        saveBtn.setVisibility(View.VISIBLE);
                        addItemBtn.setVisibility(View.VISIBLE);
                        deleteItemBtn.setVisibility(View.VISIBLE);
                        clearBtn.setVisibility(View.VISIBLE);
                        itemTxt.setVisibility(View.VISIBLE);
                        priceTxt.setVisibility(View.VISIBLE);
                        itemList.setVisibility(View.VISIBLE);
                        priceList.setVisibility(View.VISIBLE);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapterItem = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItem);
        itemList.setAdapter(adapterItem);
        adapterPrice = new ArrayAdapter<Double>(this, android.R.layout.simple_list_item_1, listPrice);
        priceList.setAdapter(adapterPrice);

        cancelBtn.setOnClickListener(v -> {
            finish();
        });

//        Loads the configuration stored in the database
        loadBtn.setOnClickListener(v -> {
            if (service.getType() == 0)
                serviceDropdown.setSelection(0);
            else if (service.getType() == 1) {
                serviceDropdown.setSelection(1);

                saveBtn.setVisibility(View.VISIBLE);
                addItemBtn.setVisibility(View.VISIBLE);
                deleteItemBtn.setVisibility(View.VISIBLE);
                clearBtn.setVisibility(View.VISIBLE);
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
            }

        });

        saveBtn.setOnClickListener(v -> {
            service.saveItems(listItem,listPrice,1);
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

//        Clear the input text
        clearBtn.setOnClickListener(v -> {
            itemTxt.getText().clear();
            priceTxt.getText().clear();
        });

//        Opens the service at current location
        providerSetupBtn.setOnClickListener(v -> {
            service.setLatitude(latitude);
            service.setLongitude(longitude);
            service.setType(type);
            service.setUpService();

            setResult(5);
            finish();

        });
    }

}
