package com.proxy.server.primenumbersserver;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.grpc.PrimeNumbersResponse;
import com.primenumbers.server.grpc.PrimeNumbersServiceClient;
import com.primenumbers.server.grpc.ReadNumbersRequest;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;

public class PrimeNumbersProtocol {
    private static final Logger log = LoggerFactory.getLogger(PrimeNumbersProtocol.class);
    private Materializer materializer;
    private PrimeNumbersServiceClient client;

    public PrimeNumbersProtocol(Materializer materializer, PrimeNumbersServiceClient client) {
        this.materializer = materializer;
        this.client = client;
    }
    
    public Tuple2<Option<String>, Source<PrimeNumbersResponse, NotUsed>> readItem(Integer number) {
        final Source<PrimeNumbersResponse, NotUsed> source = client.sendPrimeNumbersStream(ReadNumbersRequest.newBuilder()
            .setUpperBound(number).build()
        );
        
        boolean debug = true; // TODO: Get from config
        if(debug) {
            source.runForeach(r -> {
                log.debug("Received from stream: {}", r);
            }, materializer);
        }
        
        PrimeNumbersResponse firstElement;
        
        try {
            firstElement = source.take(1).runWith(Sink.head(), materializer)
                .toCompletableFuture().get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return Tuple.of(Option.of("Server Error!"), Source.empty());
        }
        
        // Read the first item and then reply based on that.. either an error or the 
        boolean valid = firstElement.getValidationError().isEmpty();
            
        return valid ? Tuple.of(Option.none(), source) : Tuple.of(Option.of(firstElement.getValidationError()), Source.empty());        
    }
}
