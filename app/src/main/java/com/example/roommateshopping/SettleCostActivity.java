package com.example.roommateshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettleCostActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter recyclerAdapter;
    private List<RecentPurchase> purchaseList;
    private List<String> roommates;
    private SettleCost settleCost;
    private TextView totalCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settle_cost);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        totalCost = findViewById(R.id.totalPrice);

        purchaseList = new ArrayList<>();
        roommates = new ArrayList<>();
        settleCost = new SettleCost();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users");

        // Grab list of user emails
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String user = postSnapshot.getValue(String.class);
                    roommates.add(user);
                }
                calculate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Grab all purchased items and call helper methods to calculate the
    // average, total spent, and amount spent per roommate
    private void calculate() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("purchasedItems");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RecentPurchase item = postSnapshot.getValue(RecentPurchase.class);
                    purchaseList.add(item);
                }

                getTotalByRoommate();
                getAverageByRoommate();

                totalCost.setText("Total Spent: $" + Math.round(settleCost.getTotalCost() * 100.0) / 100.0);

                recyclerAdapter = new SettleCostActivityAdapter(roommates, settleCost);
                recyclerView.setAdapter(recyclerAdapter);

                clearPurchases();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("RecentlyPurchased:dbRef", error.getMessage());
            }
        });
    }

    // Instead of returning to empty purchases list, go to shopping list
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }

    private void clearPurchases() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("purchasedItems");
        dbRef.removeValue();
    }

    // Go through each purchase and add up amount spent for each roommate
    private void getTotalByRoommate() {
        Map<String, Double> costByRoommate = new HashMap<>();

        for (String roommate : roommates) {
            costByRoommate.put(roommate, 0.0);
        }

        for (RecentPurchase purchase : purchaseList) {
            Double beforeVal = costByRoommate.get(purchase.getPurchasedBy());
            Double price = Double.parseDouble(purchase.getPrice());
            costByRoommate.put(purchase.getPurchasedBy(), beforeVal + price);
        }

        settleCost.setCostByRoommate(costByRoommate);
    }

    // Total amount spent divided by number of roommates
    private void getAverageByRoommate() {
        Double total = 0.0;

        for (RecentPurchase purchase : purchaseList) {
            total += Double.parseDouble(purchase.getPrice());
        }

        settleCost.setTotalCost(total);
        settleCost.setAverageCost(Math.round((total / roommates.size()) * 100.0) / 100.0);
    }

}
