package com.alten.ecommerce.models;


import lombok.Getter;

@Getter
public enum InventoryStatus {

    INSTOCK("In Stock"),
    LOWSTOCK("Low Stock"),
    OUTOFSTOCK("Out of Stock");

    private final String displayName;

    InventoryStatus(String displayName) {
        this.displayName = displayName;
    }

}
