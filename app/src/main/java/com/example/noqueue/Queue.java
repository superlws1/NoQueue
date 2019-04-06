package com.example.noqueue;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.noqueue.Firebase.Service;
import com.example.noqueue.Firebase.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.min;

/**
 * Class that allows users to see their applied service
 */
public class Queue extends AppCompatActivity {

    Button doneBtn;

    ListView serviceList;
    ArrayList<String> itemArray = new ArrayList<String>();
    ArrayList<String> serviceArray = new ArrayList<String>();
    ArrayAdapter<String> itemAdapter;

    ArrayList<String> services = new ArrayList<>();
    ArrayList<String> receipt = new ArrayList<>();
    ArrayList<String> receiptCheck = new ArrayList<>();
    ArrayList<String> diffReceipt = new ArrayList<>();
    Map<String, String> keyToService = new HashMap<>();

    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        doneBtn = findViewById(R.id.doneBtn);
        serviceList = findViewById(R.id.serviceList);

//        Removes the completed service and updates the databse
        serviceList.setOnItemClickListener((parent, view, position, id) -> {
            if (position < diffReceipt.size()){
                User user = new User();
                user.removeService(diffReceipt.get(position));
                diffReceipt.remove(position);
                itemArray.remove(position);
                itemAdapter.notifyDataSetChanged();
            }

        });

//        Gets the list of applied service
        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("waiting")
                .orderByKey()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        services = new ArrayList<>();
                        receipt = new ArrayList<>();
                        keyToService = new HashMap<>();
                        for (DataSnapshot list : dataSnapshot.getChildren()) {
                            services.add(list.getValue(String.class));
                            receipt.add(list.getKey());
                            keyToService.put(list.getKey(),list.getValue(String.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//        Gets the waiting queue time and sends a notification if the user is in line
        FirebaseDatabase.getInstance()
                .getReference()
                .child("service")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            itemArray = new ArrayList<>();
                            receiptCheck = new ArrayList<>();
                            serviceArray = new ArrayList<>();
                            for (DataSnapshot serviceList : dataSnapshot.getChildren()) {
                                if (services.contains(serviceList.getKey())){
                                    int queueNo = 0;
                                    for (DataSnapshot queue: serviceList.child("queue").getChildren()){
                                        if (receipt.contains(queue.getKey())){
                                            if (queueNo == 0)
                                                itemArray.add(serviceList.getKey().concat(" You are next."));
                                            else
                                                itemArray.add(serviceList.getKey().concat(" Waiting in queue: ").concat(String.valueOf(queueNo).concat("person(s)")));
                                            serviceArray.add(serviceList.getKey());
                                            receiptCheck.add(queue.getKey());
                                        }
                                        queueNo++;
                                    }
                                }
                            }
                            diffReceipt = new ArrayList<>(receipt);
                            diffReceipt.removeAll(receiptCheck);
                            if (diffReceipt.size() != 0) {
                                for (int i = diffReceipt.size() - 1; i >= 0; i --){
                                    String serviceName = keyToService.get(diffReceipt.get(i));
                                    itemArray.add(0,"ID: ".concat(diffReceipt.get(i)).concat(" Collect at ").concat(serviceName).concat(" Click to remove"));
                                    callNotification(serviceName);
                                }
                            }
                            itemAdapter = new ArrayAdapter<>(Queue.this,android.R.layout.simple_list_item_1,itemArray);
                            serviceList.setAdapter(itemAdapter);
                        }
                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError) {
                    }
                });


        doneBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainMenu.class);
            startActivity(intent);
        });

    }

//    Creates the notification for notifying the user in case they went out of their app
    public void callNotification(String serviceName)
    {
        Intent notificationIntent = new Intent(this, Queue.class);

        notificationBuilder = new NotificationCompat.Builder(this,"CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(serviceName)
                .setContentText("Collect your order now.")
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1,notificationBuilder.build());

    }

}
