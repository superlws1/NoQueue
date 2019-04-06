package com.example.noqueue.Firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that contains the information about the service provided
 */
public class Service {

    private int type;

    private double latitude;
    private double longitude;

    ArrayList<String> item;
    ArrayList<Double> price;

    private DatabaseReference database;
    private FirebaseUser firebaseUser;

    public Service() {}

//    Quick-save the current provided items into the database, so it can be quick-loaded next time
    public void saveItems(ArrayList<String> item, ArrayList<Double> price, int type) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        String uid = firebaseUser.getUid();

        Map<String, Object> serviceData = new HashMap<>();
        serviceData.put("item", item);
        serviceData.put("price", price);
        serviceData.put("type", type);

        Map<String, Object> putDB = new HashMap<>();
        putDB.put("users/" + uid + "/service/", serviceData);
        database.updateChildren(putDB);
    }

//    Opens the service at the current location with the provided items (if any)
    public void setUpService() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> putDB = new HashMap<>();
        putDB.put("service/" + firebaseUser.getDisplayName() + "/type/", this.type);
        putDB.put("service/" + firebaseUser.getDisplayName() + "/latitude/", this.latitude);
        putDB.put("service/" + firebaseUser.getDisplayName() + "/longitude/", this.longitude);

        if (this.type == 1) {
            putDB.put("service/" + firebaseUser.getDisplayName() + "/item/", this.item);
            putDB.put("service/" + firebaseUser.getDisplayName() + "/price/", this.price);
        }

        database.updateChildren(putDB);
    }

//    Closes the service (deletes the service in the database)
    public void stopService() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();

        database.child("service").child(firebaseUser.getDisplayName()).removeValue();
    }

//    Sends the order to the service provider from the user
    public String orderService(String userName, String serviceName, String orderItem, int type){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> putDB = new HashMap<>();
        if (type == 1){
            putDB.put("orderItem", orderItem);
        }
        else
            putDB.put("name", userName);

        String key = database.child("service").child(serviceName).child("queue").push().getKey();

        database.child("service").child(serviceName).child("queue").child(key).updateChildren(putDB);

        return key;
    }

    public void removeTopService(String userKey) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();

        database.child("service").child(firebaseUser.getDisplayName()).child("queue").child(userKey).removeValue();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<String> getItem() {
        return item;
    }

    public void setItem(ArrayList<String> item) {
        this.item = item;
    }

    public ArrayList<Double> getPrice() {
        return price;
    }

    public void setPrice(ArrayList<Double> price) {
        this.price = price;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


}
