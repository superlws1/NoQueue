package com.example.noqueue;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.noqueue.Firebase.Service;
import com.example.noqueue.Firebase.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.min;

/**
 * The main activity can be found here, i.e. Account page, Setup page, Logout, List of services page
 */
public class MainMenu extends AppCompatActivity {

    Button logoutBtn;
    Button serviceSetupBtn;
    Button accountBtn;
    Button waitingListBtn;

    ListView listService;
    ArrayList<String> arrayService = new ArrayList<String>();
    ArrayAdapter<String> adapterService;

    Intent logoutIntent;
    Intent serviceSetupIntent;

    FirebaseAuth firebaseAuth;

    User user;
    Service service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        logoutBtn = findViewById(R.id.logoutBtn);
        serviceSetupBtn = findViewById(R.id.setupServiceBtn);
        accountBtn = findViewById(R.id.accountBtn);
        waitingListBtn = findViewById(R.id.waitingListBtn);

        listService = findViewById(R.id.listService);
        adapterService = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayService);
        listService.setAdapter(adapterService);

//        Goes to the corresponding service when clicking on the list view
        listService.setOnItemClickListener((parent, view, position, id) -> {
            String serviceName = (String) listService.getItemAtPosition(position);
            Intent intent = new Intent(MainMenu.this,OrderService.class);
            intent.putExtra("name",serviceName);
            startActivityForResult(intent,50);
        });

        logoutIntent = new Intent(this, Login.class);
        serviceSetupIntent = new Intent(this, ProviderSettings.class);

        firebaseAuth = FirebaseAuth.getInstance();
        user = new User();

//        Obtains the user information
        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot) {
                        user = dataSnapshot.getValue(User.class);

                        if (user.isProvider()) {
                            serviceSetupBtn.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(MainMenu.this, ManageService.class);
                            startActivityForResult(intent,5);
                        }
                        else {
                            serviceSetupBtn.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError) {
                    }
                });

//        Gets the nearest 10 service location from the user's location
        FirebaseDatabase.getInstance()
                .getReference()
                .child("service")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange (DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            arrayService.clear();
                            ArrayList<Double> latitudeList = new ArrayList<>();
                            ArrayList<Double> longitudeList = new ArrayList<>();
                            ArrayList<String> service= new ArrayList<>();

                            for (DataSnapshot serviceList : dataSnapshot.getChildren()) {
                                service.add(serviceList.getKey());
                                latitudeList.add(serviceList.child("latitude").getValue(Double.class));
                                longitudeList.add(serviceList.child("longitude").getValue(Double.class));
                            }

                            String[] serviceName = new String[service.size()];
                            serviceName = service.toArray(serviceName);
                            Integer[] index = sortDistance(latitudeList, longitudeList);

                            // Show 10 or less nearest service place
                            int num = min(index.length,10);
                            for (int i = 0; i < num; i++){
                                Integer nearest = index[i];
                                arrayService.add(serviceName[nearest]);
                            }

                            adapterService.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError) {
                    }
                });

//        Logouts
        logoutBtn.setOnClickListener(v -> {
            logout();
            if (firebaseAuth.getCurrentUser() == null) {
                logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
            }
        });

//        Set up "business" at the current location
        serviceSetupBtn.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putDouble("latitude", user.getLatitude());
            b.putDouble("longitude", user.getLongitude());
            serviceSetupIntent.putExtras(b);
            startActivityForResult(serviceSetupIntent, 5);
        });

//        Account page
        accountBtn.setOnClickListener(v -> {
            Intent accountIntent = new Intent(this, Account.class);
            startActivity(accountIntent);
        });

//        List of service under waiting list
        waitingListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Queue.class);
            startActivity(intent);
        });

//        Gets the permission to access GPS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET},
                            10);

            }
        }

        Intent gpsService = new Intent(this, LocationService.class);
        startService(gpsService);

    }

    public void logout() {
        user.updateProvider(false);
        Intent gpsService = new Intent(this, LocationService.class);
        stopService(gpsService);
        service = new Service();
        service.stopService();
        firebaseAuth.signOut();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent gpsService = new Intent(this, LocationService.class);
        stopService(gpsService);
        user.updateProvider(false);
        service = new Service();
        service.stopService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 5){
            if (resultCode == 5){
                user.updateProvider(true);
                Intent intent = new Intent(this, ManageService.class);
                startActivityForResult(intent,5);
            }
            if (resultCode == 10){
                user.updateProvider(false);
            }
        }

        if (requestCode == 50){
            if (resultCode == 50){
                user.waitingService(data.getStringExtra("key"),data.getStringExtra("name"));
                Intent intent = new Intent(this, Queue.class);
                startActivity(intent);
            }

        }
    }

//    Sort the service location according to the user location and returns the sorted index
    private Integer[] sortDistance(ArrayList<Double> latitudeList, ArrayList<Double> longitudeList) {
        int size = latitudeList.size();
        Integer[] diff = new Integer[size];
        for (int i = 0; i < size; i++){
            diff[i] = calculateDistanceInMeter(user.getLatitude(),user.getLongitude(),
                    latitudeList.get(i),longitudeList.get(i));
        }

        ArrayIndexComparator comparator = new ArrayIndexComparator(diff);
        Integer[] indexes = comparator.createIndexArray();
        Arrays.sort(indexes, comparator);

        return indexes;
    }

//    Calculates the distance using latitude and longitude
    public int calculateDistanceInMeter(double userLat, double userLng,
                                        double venueLat, double venueLng) {
        final double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c) * 1000);
    }
}
