package com.example.roommateshopping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;
    private Button loginBtn;
    private EditText emailInput;
    private EditText passwordInput;
    private TextView signUpTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        signUpTxt = findViewById(R.id.signUpTxt);
        signUpTxt.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (auth.getCurrentUser() != null) {
            // Redirect to shopping list, user already signed in
            openShoppingList();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.signUpTxt) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            return;
        }

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (email.length() == 0 || password.length() == 0) {
            Toast.makeText(getApplicationContext(),"Enter username and password.", Toast.LENGTH_SHORT).show();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getApplicationContext(),"Valid email format required", Toast.LENGTH_SHORT).show();
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in successful
                            // Redirect to shopping list
                            openShoppingList();
                        } else {
                            // Sign in failure
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    private void openShoppingList() {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }

}