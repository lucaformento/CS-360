package com.example.weighttracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Main data screen. Reads the weight grid from the database, supports editing
 * and deleting rows, opens the add and goal screens, and sends an SMS goal
 * alert when the user has granted SMS permission.
 */
public class GridActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    // Demo destination for the goal alert. A production app would use the
    // user's saved number; the emulator can send to its own port number.
    private static final String NOTIFY_NUMBER = "5551234567";

    private DatabaseHelper db;
    private String username;
    private RecyclerView recyclerView;
    private TextView goalLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        goalLabel = findViewById(R.id.goalLabel);
        recyclerView = findViewById(R.id.weightGrid);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add opens the entry screen in "new" mode
        FloatingActionButton addButton = findViewById(R.id.buttonAdd);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddWeightActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        // Set Goal opens the goal screen
        Button goalButton = findViewById(R.id.buttonSetGoal);
        goalButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SetGoalActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        // SMS button asks for permission before any message can be sent
        Button smsButton = findViewById(R.id.buttonEnableSms);
        smsButton.setOnClickListener(v -> checkSmsPermission());
    }

    // Reload the grid every time the screen returns to the foreground
    @Override
    protected void onResume() {
        super.onResume();
        loadGrid();
    }

    // Read the entries and goal from the database and bind them to the grid
    private void loadGrid() {
        List<Weight> weights = db.getWeights(username);
        double goal = db.getGoal(username);
        goalLabel.setText(goal < 0 ? "Goal: not set" : "Goal: " + goal + " lbs");

        WeightAdapter adapter = new WeightAdapter(weights, new WeightAdapter.RowListener() {
            @Override
            public void onEdit(Weight weight) {
                // Open the entry screen pre-filled for editing
                Intent intent = new Intent(GridActivity.this, AddWeightActivity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("WEIGHT_ID", weight.getId());
                intent.putExtra("WEIGHT_VALUE", weight.getValue());
                intent.putExtra("WEIGHT_DATE", weight.getDate());
                startActivity(intent);
            }

            @Override
            public void onDelete(Weight weight) {
                db.deleteWeight(weight.getId());
                loadGrid();
            }
        });
        recyclerView.setAdapter(adapter);

        checkGoalReached(weights, goal);
    }

    // If the newest weight is at or below the goal, send an SMS alert (when allowed)
    private void checkGoalReached(List<Weight> weights, double goal) {
        if (goal < 0 || weights.isEmpty()) {
            return;
        }
        double latest = weights.get(0).getValue();
        if (latest <= goal && hasSmsPermission()) {
            sendGoalSms(latest);
        }
    }

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Ask for SEND_SMS if we do not already have it
    private void checkSmsPermission() {
        if (hasSmsPermission()) {
            Toast.makeText(this, "SMS alerts are enabled", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }
    }

    // React to the user's choice; the app keeps working either way
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS goal alerts enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Alerts off. The app still works without SMS.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Send the goal-reached alert as an SMS
    private void sendGoalSms(double latest) {
        String alert = "Congratulations! You reached your goal weight of " + latest + " lbs.";
        try {
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }
            smsManager.sendTextMessage(NOTIFY_NUMBER, null, alert, null, null);
            Toast.makeText(this, "Goal reached! SMS alert sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // Never let a messaging failure crash the app
            Toast.makeText(this, "Goal reached! (SMS could not be sent)", Toast.LENGTH_LONG).show();
        }
    }
}
