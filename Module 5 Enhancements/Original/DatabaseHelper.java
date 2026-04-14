package com.example.cs350inventoryappprojectzacharychan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

// DatabaseHelper manages SQLite database for users and inventory
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database configuration
    private static final String DATABASE_NAME = "InventoryApp.db";
    private static final int DATABASE_VERSION = 1;

    // Users table and columns
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Inventory table and columns
    private static final String TABLE_INVENTORY = "inventory";
    private static final String COLUMN_ITEM_ID = "id";
    private static final String COLUMN_ITEM_NAME = "item_name";
    private static final String COLUMN_ITEM_QUANTITY = "quantity";

    // Constructor initializes the database
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        // Create inventory table
        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " (" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ITEM_NAME + " TEXT, " +
                COLUMN_ITEM_QUANTITY + " INTEGER)";
        db.execSQL(createInventoryTable);
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop existing tables and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // ===== USER METHODS =====

    // Inserts a new user with given username and password
    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_USERS, null, values);
        } catch (Exception e) {
            e.printStackTrace(); // Log insert failure
        }

        return result != -1; // Return true if insert succeeded
    }

    // Checks if a user with given username and password exists
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null
        );

        boolean exists = cursor.moveToFirst(); // True if at least one match
        cursor.close();
        return exists;
    }

    // ===== INVENTORY METHODS =====

    // Inserts a new inventory item
    public boolean insertItem(String itemName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_ITEM_QUANTITY, quantity);
        long result = db.insert(TABLE_INVENTORY, null, values);
        return result != -1;
    }

    // Updates the quantity of an inventory item by ID
    public boolean updateItemQuantity(int id, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_QUANTITY, newQuantity);
        int result = db.update(TABLE_INVENTORY, values, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0; // True if at least one row was updated
    }

    // Deletes an inventory item by ID
    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_INVENTORY, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0; // True if at least one row was deleted
    }

    // Retrieves the full inventory list as an ArrayList
    public ArrayList<InventoryItem> getInventoryItemList() {
        ArrayList<InventoryItem> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INVENTORY, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY));
                itemList.add(new InventoryItem(id, name, quantity));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemList;
    }
}
