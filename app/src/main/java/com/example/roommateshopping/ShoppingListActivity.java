package com.example.roommateshopping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {

    private ListView listView;
    private ImageButton addButton;
    private EditText addItemInput;
    private ShoppingListArrayAdapter adapter;
    private Button markPurchasedBtn;
    private List<String> shoppingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        listView = findViewById(R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        addButton = findViewById(R.id.addButton);
        markPurchasedBtn = findViewById(R.id.markPurchasedBtn);
        markPurchasedBtn.setVisibility(View.INVISIBLE);

        addItemInput = findViewById(R.id.editText1);

        shoppingList = new ArrayList<>();
        getShoppingList();

        markPurchasedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> itemsSelected = new ArrayList<>();

                for (Integer position : adapter.getPositionsSelected()) {
                    itemsSelected.add(adapter.getItem(position));
                }

                enterPrice(itemsSelected);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int length = addItemInput.getText().length();
                if (length < 3) {
                    Toast.makeText(getApplicationContext(),"Invalid. 3 characters or more.",Toast.LENGTH_SHORT).show();
                }
                else {
                    addItemToShoppingList(addItemInput.getText().toString());
                }
                addItemInput.setText("");

            }
        });

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                } else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signOut: {
                FirebaseAuth.getInstance().signOut();
            }
            case R.id.viewPurchases: {
                Intent intent = new Intent(ShoppingListActivity.this, RecentPurchaseActivity.class);
                startActivity(intent);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }

    private void getShoppingList() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("shoppingList");

        adapter = new ShoppingListArrayAdapter(ShoppingListActivity.this, shoppingList);
        listView.setAdapter(adapter);

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String item = snapshot.getValue(String.class);
                shoppingList.add(item);
                adapter.notifyDataSetChanged();
                resetListState();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adapter.notifyDataSetChanged();
                resetListState();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String item = snapshot.getValue(String.class);
                shoppingList.remove(item);
                adapter.notifyDataSetChanged();
                resetListState();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void enterPrice(List<String> itemsSelected) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Confirm Purchase");
        alert.setMessage("Enter price");

        // Set an EditText view to get user input
        EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String priceEntered = input.getText().toString();

                if (priceEntered.equals("")) {
                    Toast.makeText(getApplicationContext(), "Must enter value!", Toast.LENGTH_SHORT).show();
                    // enterPrice(itemClickedName);
                } else {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    RecentPurchase itemPurchased = new RecentPurchase(itemsSelected, priceEntered, auth.getCurrentUser().getEmail());
                    addPurchasedItems(itemPurchased);
                }
            }
        });
        
        alert.show();
    }

    private void updateName(String itemName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Update name");
        alert.setMessage("Enter name");

        // Set an EditText view to get user input
        EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newName = input.getText().toString();

                if (newName.equals("")) {
                    Toast.makeText(getApplicationContext(), "Must enter value!", Toast.LENGTH_SHORT).show();
                } else {
                    updateShoppingListItem(itemName, newName);
                }
            }
        });

        alert.show();
    }

    private void updateShoppingListItem(String itemName, String newItemName) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("shoppingList");

        dbRef.orderByValue().equalTo(itemName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    dbRef.child(child.getKey()).setValue(newItemName);
                }
                shoppingList.set(shoppingList.indexOf(itemName), newItemName);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void resetListState() {
        adapter.setPositionsSelected(new ArrayList<>());
        markPurchasedBtn.setVisibility(View.INVISIBLE);
    }

    public void addItemToShoppingList(String itemName) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("shoppingList");

        dbRef.push().setValue(itemName)
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed add item to shopping list", Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void removeFromShoppingList(String itemName) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("shoppingList");

        dbRef.orderByValue().equalTo(itemName).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void addPurchasedItems(RecentPurchase purchase) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("purchasedItems");

        String key = dbRef.push().getKey();
        purchase.setId(key);

        dbRef.child(key).setValue(purchase)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        for (String itemName : purchase.getItems()) {
                            removeFromShoppingList(itemName);
                        }
                        resetListState();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to add item to recently purchased list", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class ShoppingListArrayAdapter extends ArrayAdapter<String> {

        private final Context context;
        private List<String> itemsList;
        private List<Integer> positionsSelected;

        public ShoppingListArrayAdapter(Context context, List<String> items) {
            super(context, -1, items);
            this.context = context;
            this.itemsList = items;
            this.positionsSelected = new ArrayList<>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.shopping_list_item, parent, false);

            TextView itemName = rowView.findViewById(R.id.itemName);
            CheckBox itemCheckbox = rowView.findViewById(R.id.checkbox);
            ImageView remove = rowView.findViewById(R.id.remove);
            ImageView edit = rowView.findViewById(R.id.edit);

            itemName.setText(itemsList.get(position));

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeFromShoppingList(itemsList.get(position));
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateName(itemsList.get(position));
                }
            });

            itemCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (positionsSelected.contains(position)) positionsSelected.remove(positionsSelected.indexOf(position));
                    else positionsSelected.add(position);

                    if (positionsSelected.size() > 0) markPurchasedBtn.setVisibility(View.VISIBLE);
                    else markPurchasedBtn.setVisibility(View.INVISIBLE);
                }
            });

            return rowView;
        }

        public List<Integer> getPositionsSelected() {
            return positionsSelected;
        }

        public void setPositionsSelected(List<Integer> positionsSelected) {
            this.positionsSelected = positionsSelected;
        }
    }
}


