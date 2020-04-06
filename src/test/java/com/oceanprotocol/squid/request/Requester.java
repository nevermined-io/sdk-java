package com.oceanprotocol.squid.request;

import com.oceanprotocol.squid.request.executors.Executor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Requester {


    private static final Logger log = LogManager.getLogger(Requester.class);

    private RequesterConfigurator configurator;
    private Executor executor;
    private Statistics statistics = new Statistics();
    private Long lastPrint;

    private ExecutorService executorService;

    public Requester(RequesterConfigurator configurator, Executor executor){

        this.configurator = configurator;
        this.executor = executor;
        this.executorService = Executors.newFixedThreadPool(configurator.getConcurrentCalls());
    }


    private synchronized  void printStatistics(){

        Long currentTime = System.currentTimeMillis();
        Boolean isFinal = statistics.getTotalRequests() == configurator.getTotalRequest();

        if (currentTime - lastPrint > configurator.getPrint()||isFinal) {

            if (isFinal) log.info("FINAL RESULTS!!");
            log.info("Executions: " + statistics.getTotalRequests() + ". Succeeded:  " + statistics.getSucceeded() + ". Failed: " + statistics.getFailed() + ". Failed By Exceptions: " + statistics.getFailedByExceptions());
            Long time = (System.currentTimeMillis() - statistics.getStartTime()) / 1000;
            log.info("Ratio per second: " + (double) statistics.getTotalRequests() / time);

            lastPrint = currentTime;
        }

    }

    private void sleep(Integer executions) {

        if (executions % configurator.getRequestPerBlock() == 0){

            log.info("Sleeping...");

            try {
                Thread.sleep(configurator.getSleepTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {


        log.info("Setting up the Executor...");
        executor.setUp();

        statistics.setStartTime(System.currentTimeMillis());
        lastPrint = System.currentTimeMillis();

        log.info("Starting execution!");
        Integer calls = 0;

        for (int i = 0; i<this.configurator.getTotalRequest()||configurator.isInfiniteRequest();i++) {

            calls++;

            CompletableFuture.supplyAsync(() -> {

                log.info("Asynch call to Executor");

                try {
                    return executor.executeRequest();
                }catch(Exception e){
                    log.error("Exception received from executor", e);
                    statistics.addFailedByExceptions();
                    return false;
            }
            }, executorService)
                    .thenAccept( result -> {
                        if (result)
                            statistics.addSucceeded();
                        else statistics.addFailed();
                    })
                  /*  .exceptionally(ex -> {
                        statistics.addFailed();
                        return null;
                    })
                    */
                    .thenRun(() -> statistics.addRequest())
                    .thenRun(this::printStatistics)
                   ;

           sleep(calls);

        }

    }


}
