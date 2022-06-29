package com.example.roommateshopping;


import java.util.List;
import java.util.Objects;

public class RecentPurchase {

    private String id;
    private List<String> items;
    private String price;
    private String purchasedBy;

    public RecentPurchase() {}

    public RecentPurchase(List<String> items, String price, String purchasedBy) {
        this.items = items;
        this.price = price;
        this.purchasedBy = purchasedBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPurchasedBy() {
        return purchasedBy;
    }

    public void setPurchasedBy(String purchasedBy) {
        this.purchasedBy = purchasedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecentPurchase that = (RecentPurchase) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
