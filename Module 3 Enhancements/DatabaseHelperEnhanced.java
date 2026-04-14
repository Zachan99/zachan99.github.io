package com.example.cs350inventoryappprojectzacharychan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * DatabaseHelper manages the SQLite database for users and inventory.
 * Enhancement: improved validation, safer update behavior, and better method structure.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)";

        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " (" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ITEM_NAME + " TEXT, " +
                COLUMN_ITEM_QUANTITY + " INTEGER)";

        db.execSQL(createUsersTable);
        db.execSQL(createInventoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // ===== USER METHODS =====

    /**
     * Enhancement: validates user data before database insert.
     */
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

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // ===== INVENTORY METHODS =====

    /**
     * Enhancement: validates item input before insert.
     */
    public boolean insertItem(String itemName, int quantity) {
        if (itemName == null || itemName.trim().isEmpty() || quantity < 0) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName.trim());
        values.put(COLUMN_ITEM_QUANTITY, quantity);

        long result = db.insert(TABLE_INVENTORY, null, values);
        return result != -1;
    }

    /**
     * Enhancement: updates both item name and quantity for better maintainability.
     */
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

    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_INVENTORY, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

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
