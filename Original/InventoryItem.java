package com.example.cs350inventoryappprojectzacharychan;

/**
 * InventoryItem is a model class representing a single inventory item.
 * It holds the item's ID, name, and quantity.
 */
public class InventoryItem {
    private int id;         // Unique identifier for the item
    private String name;    // Name of the inventory item
    private int quantity;   // Quantity available in stock

    /**
     * Constructor to initialize an InventoryItem object
     *
     * @param id       Unique ID for the item (usually assigned by the database)
     * @param name     Name of the item
     * @param quantity Quantity of the item in stock
     */
    public InventoryItem(int id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    // Getter for item ID
    public int getId() {
        return id;
    }

    // Getter for item name
    public String getName() {
        return name;
    }

    // Getter for item quantity
    public int getQuantity() {
        return quantity;
    }
}
