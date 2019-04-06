package com.example.noqueue;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.noqueue.Firebase.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Start page that handles the login requests to the app
 */
public class Login extends AppCompatActivity {

    Button registerBtn;
    Button loginBtn;
    EditText emailTxt;
    EditText passTxt;

    Intent intent;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerBtn = findViewById(R.id.registerBtn);
        loginBtn = findViewById(R.id.loginBtn);
        emailTxt = findViewById(R.id.emailTxt);
        passTxt = findViewById(R.id.passTxt);

        intent = new Intent(this, MainMenu.class);

        firebaseAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Register.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(v -> {
            login(emailTxt.getText().toString(),passTxt.getText().toString());

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

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null){
            startActivity(intent);
        }
    }

    public void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        startActivity(intent);
                    else
                        Toast.makeText(this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                });
    }
}
