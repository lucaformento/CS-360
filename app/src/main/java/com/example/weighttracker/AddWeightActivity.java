package com.example.weighttracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Add or edit a single weight entry. The same screen handles both: if a
 * WEIGHT_ID is passed in, it updates that row, otherwise it inserts a new one.
 */
public class AddWeightActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private String username;
    private int weightId = -1; // -1 means we are adding, not editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_weight);

        db = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        TextView title = findViewById(R.id.addTitle);
        EditText weightField = findViewById(R.id.inputWeight);
        EditText dateField = findViewById(R.id.inputDate);
        Button save = findViewById(R.id.buttonSave);
        Button cancel = findViewById(R.id.buttonCancel);

        // If we were given an id, pre-fill the fields for editing
        if (getIntent().hasExtra("WEIGHT_ID")) {
            weightId = getIntent().getIntExtra("WEIGHT_ID", -1);
            weightField.setText(String.valueOf(getIntent().getDoubleExtra("WEIGHT_VALUE", 0)));
            dateField.setText(getIntent().getStringExtra("WEIGHT_DATE"));
            title.setText("Edit Weight");
        }

        save.setOnClickListener(v -> {
            String weightText = weightField.getText().toString().trim();
            String date = dateField.getText().toString().trim();

            if (TextUtils.isEmpty(weightText) || TextUtils.isEmpty(date)) {
                Toast.makeText(this, "Enter a weight and date", Toast.LENGTH_SHORT).show();
                return;
            }

            double weight = Double.parseDouble(weightText);
            if (weightId == -1) {
                db.addWeight(username, weight, date);   // create
            } else {
                db.updateWeight(weightId, weight, date); // update
            }
            finish(); // return to the grid, which reloads in onResume
        });

        cancel.setOnClickListener(v -> finish());
    }
}
