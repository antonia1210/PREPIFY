package com.anto.backend.dto;

public class RecipeUpdateEvent {
    private String type;
    private int batchSize;
    private int totalCount;
    private String message;

    public RecipeUpdateEvent() {}
    public RecipeUpdateEvent(String type, int batchSize, int totalCount, String message) {
        this.type = type;
        this.batchSize = batchSize;
        this.totalCount = totalCount;
        this.message = message;
    }
    public String getType() {
        return type;
    }
    public int getBatchSize() {
        return batchSize;
    }
    public int getTotalCount() {
        return totalCount;
    }
    public String getMessage() {
        return message;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
