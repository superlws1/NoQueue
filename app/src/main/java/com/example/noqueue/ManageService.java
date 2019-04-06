package com.example.noqueue;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noqueue.Firebase.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Class for provider to manage their customers
 */
public class ManageService extends AppCompatActivity {

    Button stopBtn;
    Button nextCustBtn;

    Service service = new Service();

    ListView orderList;
    ArrayList<String> orderArray = new ArrayList<String>();
    ArrayAdapter<String> orderAdapter;
    ArrayList<String> customerKey = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_service);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        stopBtn = findViewById(R.id.stopBtn);
        nextCustBtn = findViewById(R.id.nextCustBtn);

        orderList = findViewById(R.id.orderList);

//        Shows the current customer's request on the queue
        FirebaseDatabase.getInstance()
                .getReference()
                .child("service")
                .child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())
                .child("queue")
                .orderByKey()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            customerKey = new ArrayList<>();
                            orderArray = new ArrayList<>();
                            for (DataSnapshot customer: dataSnapshot.getChildren()){
                                customerKey.add(customer.getKey());
                                orderArray.add("Order ID: ".concat(customerKey.get(customerKey.size()-1)).concat(String.format("\n")).concat(customer.child("orderItem").getValue(String.class)));

                            }
                            stopBtn.setVisibility(View.INVISIBLE);
                            nextCustBtn.setVisibility(View.VISIBLE);

                            orderAdapter = new ArrayAdapter<>(ManageService.this,android.R.layout.simple_list_item_1,orderArray);
                            orderList.setAdapter(orderAdapter);

                        }
                        else {
                            orderArray = new ArrayList<>();
                            orderArray.add("Waiting for next customer.");
                            orderAdapter = new ArrayAdapter<>(ManageService.this,android.R.layout.simple_list_item_1,orderArray);
                            orderList.setAdapter(orderAdapter);
                            stopBtn.setVisibility(View.VISIBLE);
                            nextCustBtn.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError) {
                    }
                });

//        Stop the provider service when there is no more order
        stopBtn.setOnClickListener(v -> {
            service.stopService();

            Intent intent = new Intent(this, ProviderSettings.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

//        Removes the current customer and shows the next customer on the waiting list
        nextCustBtn.setOnClickListener(v -> {
            if (customerKey != null){
                service.removeTopService(customerKey.get(0));
            }
        });
    }

}
