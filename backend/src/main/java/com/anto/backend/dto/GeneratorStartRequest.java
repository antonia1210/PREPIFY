package com.anto.backend.dto;

public class GeneratorStartRequest {
    private Integer batchSize = 3;
    private Integer interval = 5000;
    public Integer getBatchSize() {
        return batchSize;
    }
    public Integer getInterval() {
        return interval;
    }
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }
    public void setInterval(Integer interval) {
        this.interval = interval;
    }
}
