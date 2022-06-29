package com.example.roommateshopping;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SettleCostActivityAdapter extends RecyclerView.Adapter<SettleCostActivityAdapter.SettleCostHolder> {

    private List<String> roommateList;
    private SettleCost settleCost;

    public SettleCostActivityAdapter(List<String> roommateList, SettleCost settleCost) {
        this.roommateList = roommateList;
        this.settleCost = settleCost;
    }

    class SettleCostHolder extends RecyclerView.ViewHolder {

        TextView nameSettle;
        TextView averageSettle;
        TextView totalPerRoommateSettle;
        TextView total;

        public SettleCostHolder(View itemView) {
            super(itemView);

            nameSettle = itemView.findViewById(R.id.roommateSettle);
            averageSettle = itemView.findViewById(R.id.averageSettle);
            totalPerRoommateSettle = itemView.findViewById(R.id.totalPerRoommateSettle);
        }

    }

    @Override
    public SettleCostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settle_cost_row, parent, false );
        return new SettleCostHolder(view);
    }

    @Override
    public void onBindViewHolder(SettleCostHolder holder, int position) {
        String roommate = roommateList.get(position);
        holder.nameSettle.setText(roommate);
        String rounded = String.valueOf(Math.round(settleCost.getCostByRoommate().get(roommate) * 100.0) / 100.0);
        holder.totalPerRoommateSettle.setText("$" + rounded);
        holder.averageSettle.setText("$" + String.valueOf(settleCost.getAverageCost()));
    }

    @Override
    public int getItemCount() {
        return roommateList.size();
    }

}
