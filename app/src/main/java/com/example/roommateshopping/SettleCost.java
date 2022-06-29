package com.example.roommateshopping;

import java.util.Map;

public class SettleCost {

    private Double averageCost;
    private Double totalCost;
    private Map<String, Double> costByRoommate;

    public SettleCost() {}

    public SettleCost(Double averageCost, Double totalCost, Map<String, Double> costByRoommate) {
        this.averageCost = averageCost;
        this.totalCost = totalCost;
        this.costByRoommate = costByRoommate;
    }

    public Double getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(Double averageCost) {
        this.averageCost = averageCost;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Map<String, Double> getCostByRoommate() {
        return costByRoommate;
    }

    public void setCostByRoommate(Map<String, Double> costByRoommate) {
        this.costByRoommate = costByRoommate;
    }
}
