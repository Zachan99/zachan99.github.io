package com.example.cs350inventoryappprojectzacharychan;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity {

    // UI elements
    private EditText itemNameInput, itemQtyInput;
    private Button addButton;
    private TableLayout tableLayout;

    // Database helper to interact with SQLite
    private DatabaseHelper dbHelper;

    // Phone number to send SMS alerts when inventory is zero
    private final String alertPhoneNumber = "1234567890"; // Replace with actual number if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Request SMS permission at runtime
        SmsHelper.requestSmsPermission(this);

        // Initialize UI elements
        itemNameInput = findViewById(R.id.editTextItemName);
        itemQtyInput = findViewById(R.id.editTextQuantity);
        addButton = findViewById(R.id.buttonAddItem);
        tableLayout = findViewById(R.id.tableLayout);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Display current inventory in table
        populateTable();

        // Add button click listener to add new inventory items
        addButton.setOnClickListener(v -> {
            String name = itemNameInput.getText().toString().trim();
            String qtyStr = itemQtyInput.getText().toString().trim();

            // Validate input
            if (!name.isEmpty() && !qtyStr.isEmpty()) {
                int quantity = Integer.parseInt(qtyStr);

                // Insert item into the database
                dbHelper.insertItem(name, quantity);

                // Clear input fields
                itemNameInput.setText("");
                itemQtyInput.setText("");

                // Refresh table
                populateTable();

                // If quantity is zero, send SMS alert
                if (quantity == 0) {
                    Toast.makeText(this, "Item '" + name + "' has zero quantity!", Toast.LENGTH_SHORT).show();
                    SmsHelper.sendSms(this, alertPhoneNumber, "Inventory Alert: '" + name + "' is out of stock!");
                }
            } else {
                Toast.makeText(this, "Enter item name and quantity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Populates the inventory table with current data from the database
    private void populateTable() {
        // Remove all rows except the header
        tableLayout.removeViews(1, Math.max(0, tableLayout.getChildCount() - 1));

        // Retrieve inventory items from the database
        ArrayList<InventoryItem> items = dbHelper.getInventoryItemList();

        for (InventoryItem item : items) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            // Display item name
            TextView nameText = new TextView(this);
            nameText.setText(item.getName());
            row.addView(nameText);

            // Display item quantity
            TextView qtyText = new TextView(this);
            qtyText.setText(String.valueOf(item.getQuantity()));
            row.addView(qtyText);

            // Delete button for each row
            Button deleteBtn = new Button(this);
            deleteBtn.setText("X");
            deleteBtn.setOnClickListener(v -> {
                dbHelper.deleteItem(item.getId());
                populateTable(); // Refresh after deletion
            });
            row.addView(deleteBtn);

            // Edit button for each row
            Button editBtn = new Button(this);
            editBtn.setText("Edit");
            editBtn.setOnClickListener(v -> showEditDialog(item));
            row.addView(editBtn);

            // Add the row to the table layout
            tableLayout.addView(row);
        }
    }

    // Displays an alert dialog to edit an existing inventory item
    private void showEditDialog(InventoryItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_item, null);
        EditText nameEdit = dialogView.findViewById(R.id.editItemNameEditText);
        EditText qtyEdit = dialogView.findViewById(R.id.editItemQuantityEditText);

        // Pre-fill with existing values
        nameEdit.setText(item.getName());
        qtyEdit.setText(String.valueOf(item.getQuantity()));

        // Build and display dialog
        new AlertDialog.Builder(this)
                .setTitle("Edit Item")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    int qty = Integer.parseInt(qtyEdit.getText().toString().trim());

                    // Update quantity in the database
                    dbHelper.updateItemQuantity(item.getId(), qty);
                    populateTable(); // Refresh after update

                    // Send SMS if updated quantity is 0
                    if (qty == 0) {
                        Toast.makeText(this, "Item '" + item.getName() + "' has zero quantity!", Toast.LENGTH_SHORT).show();
                        SmsHelper.sendSms(this, alertPhoneNumber, "Inventory Alert: '" + item.getName() + "' is out of stock!");
                    }
                })
                .setNegativeButton("Cancel", null) // Do nothing on cancel
                .show();
    }
}
