package com.example.weighttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Handles all SQLite work for the app: creating the tables and running
 * the create, read, update, and delete operations. Keeping every query
 * in this one class keeps the activities free of raw SQL.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "weighttracker.db";
    private static final int DB_VERSION = 2;
    private static final int HASH_ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;

    // users table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "_id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    // weights table
    private static final String TABLE_WEIGHTS = "weights";
    private static final String COL_WEIGHT_ID = "_id";
    private static final String COL_WEIGHT_USER = "username";
    private static final String COL_WEIGHT_VALUE = "weight";
    private static final String COL_WEIGHT_DATE = "date";

    // goals table
    private static final String TABLE_GOALS = "goals";
    private static final String COL_GOAL_ID = "_id";
    private static final String COL_GOAL_USER = "username";
    private static final String COL_GOAL_VALUE = "goalWeight";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Create the three tables the first time the database is opened
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_WEIGHTS + " (" +
                COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WEIGHT_USER + " TEXT, " +
                COL_WEIGHT_VALUE + " REAL, " +
                COL_WEIGHT_DATE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_GOALS + " (" +
                COL_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GOAL_USER + " TEXT UNIQUE, " +
                COL_GOAL_VALUE + " REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        onCreate(db);
    }

    // ---------- user account methods ----------

    // Returns true if the username already exists in the users table
    public boolean userExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USERNAME + " = ?", new String[]{username},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Saves a new user with a salted PBKDF2 hash; returns true if inserted.
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, hashPassword(password));
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Returns true only when the password verifies against the saved hash.
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_PASSWORD},
                COL_USERNAME + " = ?", new String[]{username},
                null, null, null);
        boolean match = cursor.moveToFirst()
                && verifyPassword(password, cursor.getString(0));
        cursor.close();
        return match;
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = deriveKey(password, salt, HASH_ITERATIONS);
        return HASH_ITERATIONS + ":"
                + Base64.encodeToString(salt, Base64.NO_WRAP) + ":"
                + Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    private boolean verifyPassword(String password, String storedValue) {
        try {
            String[] parts = storedValue.split(":", 3);
            if (parts.length != 3) {
                return false;
            }
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] expected = Base64.decode(parts[2], Base64.NO_WRAP);
            byte[] actual = deriveKey(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private byte[] deriveKey(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,
                iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password hashing is unavailable", exception);
        } finally {
            spec.clearPassword();
        }
    }

    // ---------- weight CRUD methods ----------

    // Create: add a weight entry for the given user
    public long addWeight(String username, double weight, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WEIGHT_USER, username);
        values.put(COL_WEIGHT_VALUE, weight);
        values.put(COL_WEIGHT_DATE, date);
        return db.insert(TABLE_WEIGHTS, null, values);
    }

    // Read: return every weight entry for the user, newest first
    public List<Weight> getWeights(String username) {
        List<Weight> weights = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS, null,
                COL_WEIGHT_USER + " = ?", new String[]{username},
                null, null, COL_WEIGHT_ID + " DESC");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WEIGHT_ID));
            double value = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WEIGHT_VALUE));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_WEIGHT_DATE));
            weights.add(new Weight(id, value, date));
        }
        cursor.close();
        return weights;
    }

    // Update: change the value and date of an existing entry
    public boolean updateWeight(int id, double weight, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_WEIGHT_VALUE, weight);
        values.put(COL_WEIGHT_DATE, date);
        int rows = db.update(TABLE_WEIGHTS, values,
                COL_WEIGHT_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // Delete: remove a single entry by its id
    public boolean deleteWeight(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_WEIGHTS,
                COL_WEIGHT_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // ---------- goal methods ----------

    // Insert the goal if the user has none, otherwise update the existing one
    public void setGoal(String username, double goalWeight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GOAL_USER, username);
        values.put(COL_GOAL_VALUE, goalWeight);
        // CONFLICT_REPLACE keeps one goal row per user because username is UNIQUE
        db.insertWithOnConflict(TABLE_GOALS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Returns the user's goal, or -1 if they have not set one
    public double getGoal(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOALS, new String[]{COL_GOAL_VALUE},
                COL_GOAL_USER + " = ?", new String[]{username},
                null, null, null);
        double goal = -1;
        if (cursor.moveToFirst()) {
            goal = cursor.getDouble(0);
        }
        cursor.close();
        return goal;
    }
}
