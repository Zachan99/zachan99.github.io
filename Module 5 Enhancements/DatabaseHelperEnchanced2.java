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
    private static final int DATABASE_VERSION = 2; // Increased for database enhancement

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
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL)";

        // Enhancement: item_name is now UNIQUE to improve data integrity
        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " (" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ITEM_NAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL)";

        db.execSQL(createUsersTable);
        db.execSQL(createInventoryTable);
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // ===== USER METHODS =====

    // Inserts a new user
    public boolean insertUser(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username.trim());
        values.put(COLUMN_PASSWORD, password.trim());

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_USERS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result != -1;
    }

    // Checks if a user exists
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username.trim(), password.trim()},
                null, null, null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // ===== INVENTORY METHODS =====

    // Checks whether an item already exists by name
    public boolean itemExists(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_INVENTORY,
                new String[]{COLUMN_ITEM_ID},
                COLUMN_ITEM_NAME + "=?",
                new String[]{itemName.trim()},
                null, null, null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Retrieves a single item by name
    public InventoryItem getItemByName(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_INVENTORY,
                new String[]{COLUMN_ITEM_ID, COLUMN_ITEM_NAME, COLUMN_ITEM_QUANTITY},
                COLUMN_ITEM_NAME + "=?",
                new String[]{itemName.trim()},
                null, null, null
        );

        InventoryItem item = null;

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY));
            item = new InventoryItem(id, name, quantity);
        }

        cursor.close();
        return item;
    }

    // Inserts a new inventory item
    public boolean insertItem(String itemName, int quantity) {
        if (itemName == null || itemName.trim().isEmpty() || quantity < 0) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName.trim());
        values.put(COLUMN_ITEM_QUANTITY, quantity);

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_INVENTORY, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result != -1;
    }

    // Enhancement: insert or update an item if it already exists
    public boolean insertOrUpdateItem(String itemName, int quantity) {
        if (itemName == null || itemName.trim().isEmpty() || quantity < 0) {
            return false;
        }

        InventoryItem existingItem = getItemByName(itemName);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            return updateItem(existingItem.getId(), itemName, newQuantity);
        } else {
            return insertItem(itemName, quantity);
        }
    }

    // Updates both item name and quantity
    public boolean updateItem(int id, String newName, int newQuantity) {
        if (newName == null || newName.trim().isEmpty() || newQuantity < 0) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, newName.trim());
        values.put(COLUMN_ITEM_QUANTITY, newQuantity);

        int result = db.update(
                TABLE_INVENTORY,
                values,
                COLUMN_ITEM_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    // Deletes an inventory item by ID
    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_INVENTORY, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // Retrieves all inventory items
    public ArrayList<InventoryItem> getInventoryItemList() {
        ArrayList<InventoryItem> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Enhancement: ordered query for more consistent retrieval
        Cursor cursor = db.query(
                TABLE_INVENTORY,
                null,
                null,
                null,
                null,
                null,
                COLUMN_ITEM_NAME + " ASC"
        );

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

    // Enhancement: retrieves low-stock items for better database-driven reporting
    public ArrayList<InventoryItem> getLowStockItems(int threshold) {
        ArrayList<InventoryItem> lowStockItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_INVENTORY,
                null,
                COLUMN_ITEM_QUANTITY + "<=?",
                new String[]{String.valueOf(threshold)},
                null,
                null,
                COLUMN_ITEM_QUANTITY + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY));
                lowStockItems.add(new InventoryItem(id, name, quantity));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return lowStockItems;
    }
}