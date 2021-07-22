package com.chitranjank.apps.socialchats.Utilities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chitranjank.apps.socialchats.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginAccount extends AppCompatActivity {
    private EditText loginEmail,loginPassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_account);
        Button btnLogin = findViewById(R.id.btnLogin);
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.loginPassword);


        Toolbar toolbar = findViewById(R.id.customToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login");

        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString().trim();
                String pass = loginPassword.getText().toString();

                if (email.isEmpty() || pass.trim().isEmpty()){
                    Toast.makeText(LoginAccount.this,"All fields required"
                            ,Toast.LENGTH_SHORT).show();
                } else {
                    login_user(email,pass);
                }
            }
        });
    }

    private void login_user(String email, String pass) {
        ProgressDialog progressDialog = new ProgressDialog(LoginAccount.this);
        progressDialog.setMessage("Logging please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        auth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            Intent intent = new Intent(LoginAccount.this,HomePage.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginAccount.this,"Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}