package com.example.roommateshopping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentPurchaseRecyclerAdapter extends RecyclerView.Adapter<RecentPurchaseRecyclerAdapter.RecentPurchaseHolder> {

    private List<RecentPurchase> purchaseList;
    private OnItemClickListener listener;

    public RecentPurchaseRecyclerAdapter(List<RecentPurchase> purchaseList, OnItemClickListener onItemClickListener) {
        this.purchaseList = purchaseList;
        this.listener = onItemClickListener;
    }

    class RecentPurchaseHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView price;
        TextView purchasedBy;
        ImageView edit;
        ImageView remove;

        public RecentPurchaseHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            purchasedBy = itemView.findViewById(R.id.purchasedBy);
            edit = itemView.findViewById(R.id.edit);
            remove = itemView.findViewById(R.id.remove);
        }

    }

    public interface OnItemClickListener {
        void onItemClick(String type, RecentPurchase purchase);
    }

    @Override
    public RecentPurchaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_purchase_row, parent, false );
        return new RecentPurchaseHolder(view);
    }

    @Override
    public void onBindViewHolder(RecentPurchaseHolder holder, int position) {
        RecentPurchase purchase = purchaseList.get(position);

        String name = "";

        for (String itemName : purchase.getItems()) {
            name += itemName + ", ";
        }

        holder.name.setText(name.substring(0, name.length() - 2));
        holder.price.setText("$" + purchase.getPrice());
        holder.purchasedBy.setText(purchase.getPurchasedBy());
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                listener.onItemClick("remove", purchase);
            }
        });
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                listener.onItemClick("edit", purchase);
            }
        });
    }

    @Override
    public int getItemCount() {
        return purchaseList.size();
    }

}
