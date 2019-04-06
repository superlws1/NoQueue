package com.example.noqueue;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * User can check their basic information and change password here
 */
public class Account extends AppCompatActivity {

    Button backBtn;
    Button passwordBtn;
    Button passwordUpdateBtn;

    TextView nameView;
    TextView emailView;
    TextView oldView;
    TextView newView;

    EditText oldPassTxt;
    EditText newPassTxt;

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = findViewById(R.id.backBtn);
        passwordBtn = findViewById(R.id.passwordBtn);
        passwordUpdateBtn = findViewById(R.id.passwordUpdateBtn);

        nameView = findViewById(R.id.nameView);
        emailView = findViewById(R.id.emailView);
        oldView = findViewById(R.id.oldView);
        newView = findViewById(R.id.newView);

        oldPassTxt = findViewById(R.id.oldPassTxt);
        newPassTxt = findViewById(R.id.newPassTxt);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        nameView.setText(firebaseUser.getDisplayName());
        emailView.setText(firebaseUser.getEmail());

        backBtn.setOnClickListener(v -> {
            finish();
        });

        passwordBtn.setOnClickListener(v -> {
            oldView.setVisibility(View.VISIBLE);
            newView.setVisibility(View.VISIBLE);
            oldPassTxt.setVisibility(View.VISIBLE);
            newPassTxt.setVisibility(View.VISIBLE);
            passwordUpdateBtn.setVisibility(View.VISIBLE);
        });

//        Checks if the old password is correct, then updates user new password
        passwordUpdateBtn.setOnClickListener(v -> {
            String oldPass = oldPassTxt.getText().toString();
            String newPass = newPassTxt.getText().toString();
            AuthCredential credential = EmailAuthProvider
                    .getCredential(firebaseUser.getEmail(), oldPass);

            firebaseUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            firebaseUser.updatePassword(newPass).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(Account.this,"Password updated",Toast.LENGTH_SHORT).show();
                                    oldPassTxt.getText().clear();
                                    newPassTxt.getText().clear();
                                } else {
                                    Toast.makeText(Account.this,task1.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(Account.this,"Incorrect password",Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

}
