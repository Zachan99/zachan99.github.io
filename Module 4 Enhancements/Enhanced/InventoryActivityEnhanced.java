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
import java.util.Collections;
import java.util.Comparator;

public class InventoryActivity extends AppCompatActivity {

    // UI elements
    private EditText itemNameInput, itemQtyInput, searchInput;
    private Button addButton, searchButton, showAllButton, sortNameButton, sortQtyButton;
    private TableLayout tableLayout;

    // Database helper
    private DatabaseHelper dbHelper;

    // Phone number to send SMS alerts when inventory is zero
    private final String alertPhoneNumber = "1234567890";

    // Enhancement: keep a working list in memory for sorting/filtering
    private ArrayList<InventoryItem> currentItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        SmsHelper.requestSmsPermission(this);

        // Main inventory inputs
        itemNameInput = findViewById(R.id.editTextItemName);
        itemQtyInput = findViewById(R.id.editTextQuantity);
        addButton = findViewById(R.id.buttonAddItem);
        tableLayout = findViewById(R.id.tableLayout);

        // New UI elements for algorithms/data structure enhancement
        searchInput = findViewById(R.id.editTextSearch);
        searchButton = findViewById(R.id.buttonSearch);
        showAllButton = findViewById(R.id.buttonShowAll);
        sortNameButton = findViewById(R.id.buttonSortName);
        sortQtyButton = findViewById(R.id.buttonSortQuantity);

        dbHelper = new DatabaseHelper(this);

        // Load and display all items initially
        refreshInventoryList();

        // Add item
        addButton.setOnClickListener(v -> handleAddItem());

        // Search items using linear search/filter logic
        searchButton.setOnClickListener(v -> handleSearch());

        // Show all items again
        showAllButton.setOnClickListener(v -> refreshInventoryList());

        // Sort alphabetically by item name
        sortNameButton.setOnClickListener(v -> {
            sortItemsByName(currentItems);
            displayItems(currentItems);
        });

        // Sort numerically by quantity
        sortQtyButton.setOnClickListener(v -> {
            sortItemsByQuantity(currentItems);
            displayItems(currentItems);
        });
    }

    /**
     * Enhancement: refresh the in-memory ArrayList from the database
     * and display the full inventory.
     */
    private void refreshInventoryList() {
        currentItems = dbHelper.getInventoryItemList();
        displayItems(currentItems);
    }

    /**
     * Enhancement: separated add-item logic for maintainability.
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
            itemNameInput.setText("");
            itemQtyInput.setText("");
            refreshInventoryList();

            if (quantity == 0) {
                sendZeroQuantityAlert(name);
            }
        } else {
            Toast.makeText(this, "Unable to add item", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Enhancement: search feature using ArrayList traversal.
     * This demonstrates algorithmic thinking through filtering.
     */
    private void handleSearch() {
        String query = searchInput.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this, "Enter a name to search", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<InventoryItem> filteredItems = filterItemsByName(query, currentItems);

        if (filteredItems.isEmpty()) {
            Toast.makeText(this, "No matching items found", Toast.LENGTH_SHORT).show();
        }

        displayItems(filteredItems);
    }

    /**
     * Enhancement: linear search/filter algorithm over the ArrayList.
     */
    private ArrayList<InventoryItem> filterItemsByName(String query, ArrayList<InventoryItem> items) {
        ArrayList<InventoryItem> filteredItems = new ArrayList<>();

        for (InventoryItem item : items) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    /**
     * Enhancement: sorting algorithm using Collections.sort and Comparator.
     * Sorts inventory items alphabetically by name.
     */
    private void sortItemsByName(ArrayList<InventoryItem> items) {
        Collections.sort(items, new Comparator<InventoryItem>() {
            @Override
            public int compare(InventoryItem item1, InventoryItem item2) {
                return item1.getName().compareToIgnoreCase(item2.getName());
            }
        });
    }

    /**
     * Enhancement: sorting algorithm using Collections.sort and Comparator.
     * Sorts inventory items from lowest quantity to highest quantity.
     */
    private void sortItemsByQuantity(ArrayList<InventoryItem> items) {
        Collections.sort(items, new Comparator<InventoryItem>() {
            @Override
            public int compare(InventoryItem item1, InventoryItem item2) {
                return Integer.compare(item1.getQuantity(), item2.getQuantity());
            }
        });
    }

    /**
     * Enhancement: validation improves reliability and prevents invalid entries.
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
     * Enhancement: safe number parsing prevents crashes.
     */
    private int parseQuantity(String qtyStr) {
        try {
            return Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void sendZeroQuantityAlert(String itemName) {
        Toast.makeText(this, "Item '" + itemName + "' has zero quantity!", Toast.LENGTH_SHORT).show();
        SmsHelper.sendSms(this, alertPhoneNumber,
                "Inventory Alert: '" + itemName + "' is out of stock!");
    }

    /**
     * Enhancement: displays any provided list, including sorted or filtered results.
     */
    private void displayItems(ArrayList<InventoryItem> items) {
        tableLayout.removeViews(1, Math.max(0, tableLayout.getChildCount() - 1));

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
                refreshInventoryList();
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
     * Existing edit dialog updated to refresh the ArrayList after changes.
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
                        refreshInventoryList();

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