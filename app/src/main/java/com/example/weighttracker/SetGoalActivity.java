package com.example.weighttracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Lets the user set or update their goal weight, which is stored one row
 * per user in the goals table.
 */
public class SetGoalActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_goal);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        EditText goalField = findViewById(R.id.inputGoal);
        Button save = findViewById(R.id.buttonSaveGoal);
        Button cancel = findViewById(R.id.buttonCancelGoal);

        // Show the current goal if one is already set
        double current = db.getGoal(username);
        if (current >= 0) {
            goalField.setText(String.valueOf(current));
        }

        save.setOnClickListener(v -> {
            String goalText = goalField.getText().toString().trim();
            if (TextUtils.isEmpty(goalText)) {
                Toast.makeText(this, "Enter a goal weight", Toast.LENGTH_SHORT).show();
                return;
            }
            db.setGoal(username, Double.parseDouble(goalText));
            finish();
        });

        cancel.setOnClickListener(v -> finish());
    }
}
