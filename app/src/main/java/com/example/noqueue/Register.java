package com.example.noqueue;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.noqueue.Firebase.User;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Class that allows users to register themselves
 */
public class Register extends AppCompatActivity {

    Button backBtn;
    Button registerBtn;
    Button providerRegisterBtn;
    EditText nameTxt;
    EditText emailTxt;
    EditText passTxt;

    Intent registerIntent;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = findViewById(R.id.backHome);
        registerBtn = findViewById(R.id.registerBtn);
        providerRegisterBtn = findViewById(R.id.providerRegisterBtn);
        nameTxt = findViewById(R.id.nameTxt);
        emailTxt = findViewById(R.id.emailTxt);
        passTxt = findViewById(R.id.passTxt);

        registerIntent = new Intent(this, MainMenu.class);

        firebaseAuth = FirebaseAuth.getInstance();

        backBtn.setOnClickListener(v -> {
            finish();
        });

        registerBtn.setOnClickListener(v -> {
            User user = new User(nameTxt.getText().toString(),
                    emailTxt.getText().toString(),
                    passTxt.getText().toString());

            registerUser(user, false);

        });

        providerRegisterBtn.setOnClickListener(v -> {
            User user = new User(nameTxt.getText().toString(),
                    emailTxt.getText().toString(),
                    passTxt.getText().toString());

            registerUser(user, true);
        });

    }

    public void registerUser(User user, boolean provider) {
        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.addNewUser(user, provider);
                        startActivity(registerIntent);
                    }
                    else
                        Toast.makeText(this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                });

    }
}
