package com.oceanprotocol.squid.request;

public class Statistics {

    private volatile Integer totalRequests;
    private volatile Integer succeeded;
    private volatile Integer failed;
    private volatile Integer failedByExceptions;
    private Float requestRatio = 0f;
    private Long startTime;
    private Long endTime;

    public Statistics(){
        totalRequests = 0;
        succeeded = 0;
        failed = 0;
        failedByExceptions = 0;

    }

    public synchronized Integer addRequest(){
        return ++totalRequests;
    }

    public synchronized Integer addSucceeded() {
        return ++succeeded;
    }

    public synchronized Integer addFailed() {
        return ++failed;
    }

    public synchronized Integer addFailedByExceptions() {
        return ++failedByExceptions;
    }

    public Integer getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Integer totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Integer getSucceeded() {
        return succeeded;
    }


    public Integer getFailed() {
        return failed;
    }


    public Float getRequestRatio() {
        return requestRatio;
    }


    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getFailedByExceptions() {
        return failedByExceptions;
    }

    public void setFailedByExceptions(Integer failedByExceptions) {
        this.failedByExceptions = failedByExceptions;
    }
}
