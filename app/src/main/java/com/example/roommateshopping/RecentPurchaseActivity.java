package com.example.roommateshopping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecentPurchaseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter recyclerAdapter;

    private List<RecentPurchase> purchaseList;
    private Button settleCostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_purchase);

        settleCostButton = findViewById(R.id.settleCostButton);
        settleCostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecentPurchaseActivity.this, SettleCostActivity.class);
                startActivity(intent);
            }
        });
        settleCostButton.setVisibility(View.INVISIBLE);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        purchaseList = new ArrayList<>();

        recyclerAdapter = new RecentPurchaseRecyclerAdapter(purchaseList, new RecentPurchaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String type, RecentPurchase purchase) {
                if (type.equals("remove")) {
                    removePurchase(purchase);
                    backToShoppingList(purchase);
                    Toast.makeText(getApplicationContext(), "Removed from recent purchases and added back to shopping list", Toast.LENGTH_SHORT).show();
                } else {
                    updatePrice(purchase);
                }
            }
        });
        recyclerView.setAdapter(recyclerAdapter);

        getRecentPurchases();
    }

    private void updatePrice(RecentPurchase purchase) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Update Price");
        alert.setMessage("Enter price");

        // Set an EditText view to get user input
        EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newPrice = input.getText().toString();

                if (newPrice.equals("")) {
                    Toast.makeText(getApplicationContext(), "Must enter value!", Toast.LENGTH_SHORT).show();
                } else {
                    updatePurchasePrice(purchase, newPrice);
                }
            }
        });

        alert.show();
    }

    private void getRecentPurchases() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("purchasedItems");

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RecentPurchase purchase = snapshot.getValue(RecentPurchase.class);
                purchaseList.add(purchase);
                recyclerAdapter.notifyDataSetChanged();

                if (settleCostButton.getVisibility() == View.INVISIBLE) settleCostButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                RecentPurchase purchase = snapshot.getValue(RecentPurchase.class);
                purchaseList.get(purchaseList.indexOf(purchase)).setPrice(purchase.getPrice());
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                RecentPurchase purchase = snapshot.getValue(RecentPurchase.class);
                purchaseList.remove(purchase);
                recyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updatePurchasePrice(RecentPurchase purchase, String newPrice) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("purchasedItems");

        dbRef.orderByKey().equalTo(purchase.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    dbRef.child(child.getKey()).child("price").setValue(newPrice);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void removePurchase(RecentPurchase purchase) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("purchasedItems");

        dbRef.orderByKey().equalTo(purchase.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    dbRef.child(child.getKey()).removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void backToShoppingList(RecentPurchase purchase) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("shoppingList");

        for (String itemName : purchase.getItems()) {
            dbRef.push().setValue(itemName)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed to remove from recent purchases", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}