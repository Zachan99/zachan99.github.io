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

/**
 * InventoryActivity manages inventory item display and CRUD interactions.
 * Enhancement: improved validation, safer parsing, and cleaner method structure.
 */
public class InventoryActivity extends AppCompatActivity {

    private EditText itemNameInput, itemQtyInput;
    private Button addButton;
    private TableLayout tableLayout;

    private DatabaseHelper dbHelper;

    private final String alertPhoneNumber = "1234567890"; // Replace if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        SmsHelper.requestSmsPermission(this);

        itemNameInput = findViewById(R.id.editTextItemName);
        itemQtyInput = findViewById(R.id.editTextQuantity);
        addButton = findViewById(R.id.buttonAddItem);
        tableLayout = findViewById(R.id.tableLayout);

        dbHelper = new DatabaseHelper(this);

        populateTable();

        // Enhancement: extracted add-item workflow into a helper method
        addButton.setOnClickListener(v -> handleAddItem());
    }

    /**
     * Enhancement: separates add-item logic from onCreate for readability.
     */
    private void handleAddItem() {
        String name = itemNameInput.getText().toString().trim();
        String qtyStr = itemQtyInput.getText().toString().trim();

        if (!isValidItemInput(name, qtyStr)) {
            return;
        }

        int quantity = parseQuantity(qtyStr);
        if (quantity < 0) {
            Toast.makeText(this, "Quantity must be a valid non-negative number", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.insertItem(name, quantity);
        if (success) {
            clearItemInputs();
            populateTable();

            if (quantity == 0) {
                sendZeroQuantityAlert(name);
            }
        } else {
            Toast.makeText(this, "Unable to add item", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Enhancement: centralized validation improves maintainability.
     */
    private boolean isValidItemInput(String name, String qtyStr) {
        if (name.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(this, "Enter item name and quantity", Toast.LENGTH_SHORT).show();
            return false;
        }

        int quantity = parseQuantity(qtyStr);
        if (quantity < 0) {
            Toast.makeText(this, "Quantity must be a valid non-negative number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Enhancement: defensive programming to prevent crashes from invalid numbers.
     */
    private int parseQuantity(String qtyStr) {
        try {
            return Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Enhancement: reusable method for clearing form inputs.
     */
    private void clearItemInputs() {
        itemNameInput.setText("");
        itemQtyInput.setText("");
    }

    private void sendZeroQuantityAlert(String itemName) {
        Toast.makeText(this, "Item '" + itemName + "' has zero quantity!", Toast.LENGTH_SHORT).show();
        SmsHelper.sendSms(this, alertPhoneNumber,
                "Inventory Alert: '" + itemName + "' is out of stock!");
    }

    private void populateTable() {
        tableLayout.removeViews(1, Math.max(0, tableLayout.getChildCount() - 1));

        ArrayList<InventoryItem> items = dbHelper.getInventoryItemList();

        for (InventoryItem item : items) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            TextView nameText = new TextView(this);
            nameText.setText(item.getName());
            row.addView(nameText);

            TextView qtyText = new TextView(this);
            qtyText.setText(String.valueOf(item.getQuantity()));
            row.addView(qtyText);

            Button deleteBtn = new Button(this);
            deleteBtn.setText("X");
            deleteBtn.setOnClickListener(v -> {
                dbHelper.deleteItem(item.getId());
                populateTable();
            });
            row.addView(deleteBtn);

            Button editBtn = new Button(this);
            editBtn.setText("Edit");
            editBtn.setOnClickListener(v -> showEditDialog(item));
            row.addView(editBtn);

            tableLayout.addView(row);
        }
    }

    /**
     * Enhancement: edit dialog now validates both name and quantity
     * and updates both fields instead of quantity only.
     */
    private void showEditDialog(InventoryItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_item, null);
        EditText nameEdit = dialogView.findViewById(R.id.editItemNameEditText);
        EditText qtyEdit = dialogView.findViewById(R.id.editItemQuantityEditText);

        nameEdit.setText(item.getName());
        qtyEdit.setText(String.valueOf(item.getQuantity()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Item")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String updatedName = nameEdit.getText().toString().trim();
                    String updatedQtyStr = qtyEdit.getText().toString().trim();

                    if (!isValidItemInput(updatedName, updatedQtyStr)) {
                        Toast.makeText(this, "Update canceled: invalid input", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int updatedQty = parseQuantity(updatedQtyStr);
                    boolean success = dbHelper.updateItem(item.getId(), updatedName, updatedQty);

                    if (success) {
                        populateTable();

                        if (updatedQty == 0) {
                            sendZeroQuantityAlert(updatedName);
                        }
                    } else {
                        Toast.makeText(this, "Unable to update item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
