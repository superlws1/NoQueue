package com.example.noqueue;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noqueue.Firebase.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static java.lang.Math.min;

/**
 * Allows users to place their orders to the respective service provider
 */
public class OrderService extends AppCompatActivity {

    Button backBtn;
    Button removeAllBtn;
    Button placeOrderBtn;

    TextView totalView;
    TextView priceView;

    ListView itemList;
    ArrayList<String> itemArray = new ArrayList<String>();
    ArrayAdapter<String> itemAdapter;

    Service service;

    int[] itemTotal;

    String totalItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_service);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = findViewById(R.id.backBtn);
        removeAllBtn = findViewById(R.id.removeAllBtn);
        placeOrderBtn = findViewById(R.id.placeOrderBtn);

        totalView = findViewById(R.id.totalView);
        priceView = findViewById(R.id.priceView);

        itemList = findViewById(R.id.itemList);
        itemAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,itemArray);
        itemList.setAdapter(itemAdapter);

        String serviceName = getIntent().getExtras().getString("name");
        service = new Service();

//        Generates the list of available items provided by the service provider (if any)
        FirebaseDatabase.getInstance()
                .getReference()
                .child("service")
                .child(serviceName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            itemArray = new ArrayList<String>();
                            service = dataSnapshot.getValue(Service.class);

                            removeAllBtn.setVisibility(View.VISIBLE);
                            totalView.setVisibility(View.VISIBLE);
                            priceView.setVisibility(View.VISIBLE);
                            itemList.setVisibility(View.VISIBLE);

                            for (int i = 0; i < service.getItem().size(); i++) {
                                itemArray.add(service.getItem().get(i) + " RM" + String.format("%.2f", service.getPrice().get(i)));
                            }

                            itemTotal = new int[service.getItem().size()];
                            itemAdapter = new ArrayAdapter<>(OrderService.this,android.R.layout.simple_list_item_1,itemArray);
                            itemList.setAdapter(itemAdapter);

                        }
                        else {
                            Toast.makeText(OrderService.this,"Service closed",Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError) {
                    }
                });

//        Puts the selected items in a "shopping cart", showing the total price and the items selected
        itemList.setOnItemClickListener((parent, view, position, id) -> {
            itemTotal[position]++;
            double price = 0;
            totalItems = "";
            for (int i = 0; i < service.getItem().size(); i++) {
                if (itemTotal[i] == 0)
                    continue;
                totalItems = totalItems.concat(service.getItem().get(i)).concat(" x").concat(String.format("%d\n",itemTotal[i]));
                price = price + service.getPrice().get(i)*itemTotal[i];
            }

            String toAdd;
            if (itemTotal[position] > 1){
                String[] split = itemArray.get(position).split(" ",2);
                toAdd = split[1];
            }
            else {
                toAdd = itemArray.get(position);
            }
            itemArray.add(position,String.valueOf(itemTotal[position]).concat("x ").concat(toAdd));
            itemArray.remove(position + 1);
            itemAdapter.notifyDataSetChanged();
            priceView.setText(String.format("RM %.2f",price));
        });

        backBtn.setOnClickListener(v -> {
            finish();
        });

        removeAllBtn.setOnClickListener(v -> {
            itemTotal = new int[service.getItem().size()];
            priceView.setText("RM 0.00");
            for (int i = 0; i < itemArray.size(); i++){
                String[] split = itemArray.get(i).split(" ",2);
                itemArray.add(i,split[1]);
                itemArray.remove(i + 1);
                itemAdapter.notifyDataSetChanged();
            }
        });

//        Places the order to the service provider
        placeOrderBtn.setOnClickListener(v -> {

            String key = service.orderService(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),serviceName,totalItems);

            Intent intent = new Intent();
            intent.putExtra("key",key);
            intent.putExtra("name",serviceName);
            setResult(50, intent);
            finish();
        });

    }

}
