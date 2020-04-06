package com.oceanprotocol.squid.request;

public class RequesterConfigurator {

    private Integer totalRequest;

    private Boolean infiniteRequest = false;

    private Integer requestPerBlock;

    private Integer sleepTime;

    private Long print = 10000l;

    private Integer concurrentCalls;


    public RequesterConfigurator(Integer totalRequest, Integer requestPerBlock, Integer sleepTime, Integer concurrentCalls,  Long print) {

        this.totalRequest = totalRequest;
        this.requestPerBlock = requestPerBlock;
        this.sleepTime = sleepTime;
        this.concurrentCalls = concurrentCalls;
        this.print = print;
    }

    public RequesterConfigurator(Integer totalRequest, Integer requestPerBlock, Integer sleepTime, Integer concurrentCalls, Long print, Boolean infiniteRequest) {
        this(totalRequest, requestPerBlock, sleepTime, concurrentCalls, print);
        this.infiniteRequest = infiniteRequest;
    }


    public Integer getTotalRequest() {
        return totalRequest;
    }

    public void setTotalRequest(Integer totalRequest) {
        this.totalRequest = totalRequest;
    }

    public Boolean isInfiniteRequest() {
        return infiniteRequest;
    }

    public void setInfiniteRequest(Boolean infiniteRequest) {
        this.infiniteRequest = infiniteRequest;
    }

    public Integer getRequestPerBlock() {
        return requestPerBlock;
    }

    public void setRequestPerBlock(Integer requestPerBlock) {
        this.requestPerBlock = requestPerBlock;
    }

    public Integer getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(Integer sleepTime) {
        this.sleepTime = sleepTime;
    }

    public Long getPrint() {
        return print;
    }

    public void setPrint(Long print) {
        this.print = print;
    }

    public Integer getConcurrentCalls() {
        return concurrentCalls;
    }

    public void setConcurrentCalls(Integer concurrentCalls) {
        this.concurrentCalls = concurrentCalls;
    }
}
