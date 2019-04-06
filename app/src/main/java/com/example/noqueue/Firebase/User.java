package com.example.noqueue.Firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing the user information
 */
public class User {

    private String name;
    private String email;
    private String password;

    private double latitude;
    private double longitude;

    private DatabaseReference database;
    private FirebaseUser firebaseUser;

    private boolean provider = false;

    public User() {
    }

    public User(String name, String email, String password) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

//    Add the user information (name) to the database
    public void addNewUser(User user) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        String uid = firebaseUser.getUid();

        Map<String, Object> putDB = new HashMap<>();
        putDB.put("users/" + uid + "/name", user.name);
        database.updateChildren(putDB);

        updateProvider(false);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.name).build();
        firebaseUser.updateProfile(profileUpdates);
    }

//    Updates the user GPS in the database
    public void updateGPS(double latitude, double longitude){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        String uid = firebaseUser.getUid();

        Map<String, Object> putDB = new HashMap<>();
        putDB.put("users/" + uid + "/latitude", latitude);
        putDB.put("users/" + uid + "/longitude", longitude);
        database.updateChildren(putDB);
    }

//    Toggle whether user is a provider
    public void updateProvider(boolean provider){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        String uid = firebaseUser.getUid();

        Map<String, Object> putDB = new HashMap<>();
        putDB.put("users/" + uid + "/provider", provider);
        database.updateChildren(putDB);
    }

//    Adds the selected service provider under the waiting list
    public void waitingService(String key, String serviceName){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        String uid = firebaseUser.getUid();
        DatabaseReference ref = database.child("users").child(uid).child("waiting");

        Map<String, Object> putDB = new HashMap<>();
        putDB.put(key, serviceName);
        ref.updateChildren(putDB);
    }

//    Removes the selected service provider from the waiting list
    public void removeService(String receipt){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();

        database.child("users").child(firebaseUser.getUid()).child("waiting").child(receipt).removeValue();


    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isProvider() {
        return provider;
    }
}
