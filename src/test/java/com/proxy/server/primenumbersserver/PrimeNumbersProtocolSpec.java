package com.proxy.server.primenumbersserver;

import static org.forgerock.cuppa.Cuppa.after;
import static org.forgerock.cuppa.Cuppa.before;
import static org.forgerock.cuppa.Cuppa.describe;
import static org.forgerock.cuppa.Cuppa.it;
import static org.forgerock.cuppa.Cuppa.when;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.forgerock.cuppa.junit.CuppaRunner;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.primenumbers.server.grpc.PrimeNumbersResponse;
import com.primenumbers.server.grpc.PrimeNumbersServiceClient;
import com.primenumbers.server.grpc.ReadNumbersRequest;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.testkit.javadsl.TestKit;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.control.Option;

@RunWith(CuppaRunner.class)
public class PrimeNumbersProtocolSpec {
    private static ActorSystem system;
    private static Materializer materializer;
    private static PrimeNumbersServiceClient client;
    private static PrimeNumbersProtocol protocol;
    {
        describe("PrimeNumbersProtocolSpec", () -> {

            before(() -> {
                system = ActorSystem.create("PrimeNumberGeneratorSpec");
                materializer = ActorMaterializer.create(system);
                client = Mockito.mock(PrimeNumbersServiceClient.class);
                protocol = new PrimeNumbersProtocol(materializer, client);
            });

            after("Shutdown", () -> {
                TestKit.shutdownActorSystem(system);
                system = null;
                protocol = null;
            });

            when("we call the protocol to read a prime number series with a valid integer number", () -> {
                it("should return the prime number series", () -> {
                    ReadNumbersRequest req = ReadNumbersRequest.newBuilder().setUpperBound(10).build();
                    Set<PrimeNumbersResponse> set = HashSet.of(
                        PrimeNumbersResponse.newBuilder().setPrimeNumber(2).build(),
                        PrimeNumbersResponse.newBuilder().setPrimeNumber(3).build(),
                        PrimeNumbersResponse.newBuilder().setPrimeNumber(5).build(),
                        PrimeNumbersResponse.newBuilder().setPrimeNumber(7).build()); 
                    Source<PrimeNumbersResponse, NotUsed> source = Source.from(set);
                    Mockito.when(client.sendPrimeNumbersStream(req)).thenReturn(source);
                    
                    Tuple2<Option<String>, Source<Integer, NotUsed>> response = protocol.readItem(10);
                    
                    Source<Integer, NotUsed> streamSource = response._2();
                    final CompletionStage<List<Integer>> future =
                        streamSource.runWith(Sink.seq(), materializer);
                    HashSet<Integer> result = HashSet.ofAll(future.toCompletableFuture().get());
                    assertTrue(result.size() == 4);
                    assertTrue(response._1().isEmpty());
                    assertTrue(result.containsAll(HashSet.of(2,3,5,7)));
                });
            });
            
            when("we call the protocol to read a prime number series with an invalid integer number", () -> {
                it("should return a validation error", () -> {
                    ReadNumbersRequest req = ReadNumbersRequest.newBuilder().setUpperBound(-10).build();
                    Set<PrimeNumbersResponse> set = HashSet.of(
                        PrimeNumbersResponse.newBuilder().setValidationError("Invalid Input").build()); 
                    Source<PrimeNumbersResponse, NotUsed> source = Source.from(set);
                    Mockito.when(client.sendPrimeNumbersStream(req)).thenReturn(source);
                    
                    Tuple2<Option<String>, Source<Integer, NotUsed>> response = protocol.readItem(-10);
                    
                    Source<Integer, NotUsed> streamSource = response._2();
                    final CompletionStage<List<Integer>> future =
                        streamSource.runWith(Sink.seq(), materializer);
                    HashSet<Integer> result = HashSet.ofAll(future.toCompletableFuture().get());
                    assertTrue(result.isEmpty());
                    assertTrue(response._1().isDefined());
                    assertTrue(response._1().get().equals("Invalid Input"));
                });
            });
        });
    }}