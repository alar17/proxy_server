package com.proxy.server.primenumbersserver;

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
    
    public Tuple2<Option<String>, Source<Integer, NotUsed>> readItem(Integer number) {
        final Source<PrimeNumbersResponse, NotUsed> source = client.sendPrimeNumbersStream(ReadNumbersRequest.newBuilder()
            .setUpperBound(number).build()
        );
        
        boolean debug = false; // TODO: Get from config
        if(debug) {
            source.runForeach(r -> {
                log.debug("Received from stream: {}", r);
            }, materializer);
        }
        
        PrimeNumbersResponse firstElement;
        
        try {
            firstElement = source.take(1).runWith(Sink.head(), materializer)
                .toCompletableFuture().get(10, TimeUnit.SECONDS); // If we don't receive the first element in 10 seconds, we will return the server error
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return Tuple.of(Option.of("Server Error!"), Source.empty());
        }
        
        // Read the first item and then reply based on that.. either an error or the 
        boolean valid = firstElement.getValidationError().isEmpty();
            
        return valid ?
            Tuple.of(Option.none(), source.map(item -> item.getPrimeNumber())) : // Returns the validation error
            Tuple.of(Option.of(firstElement.getValidationError()), Source.empty()); // Returns the integer stream
    }
}
