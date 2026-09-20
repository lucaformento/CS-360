package com.example.weighttracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Login screen. Checks credentials against the database and lets a first-time
 * user create an account that is saved to the users table.
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        EditText usernameField = findViewById(R.id.usernameText);
        EditText passwordField = findViewById(R.id.passwordText);
        Button loginButton = findViewById(R.id.buttonLogin);
        Button createButton = findViewById(R.id.buttonCreate);
        TextView message = findViewById(R.id.messageText);

        // Log in: only continue when the credentials match a saved user
        loginButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                message.setText("Please enter a username and password");
                return;
            }

            if (db.checkUser(username, password)) {
                openGrid(username);
            } else {
                message.setText("Invalid login. Try again or create an account.");
            }
        });

        // Create account: save the new user, then continue into the app
        createButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                message.setText("Please enter a username and password");
                return;
            }

            if (db.userExists(username)) {
                message.setText("That username is taken. Please log in.");
            } else if (db.addUser(username, password)) {
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                openGrid(username);
            } else {
                message.setText("Could not create account. Try again.");
            }
        });
    }

    // Move to the grid, passing the username so it loads the right data
    private void openGrid(String username) {
        Intent intent = new Intent(MainActivity.this, GridActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }
}
