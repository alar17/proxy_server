package com.proxy.server.directives;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proxy.server.primenumbersserver.PrimeNumbersProtocol;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

/**
 * Directives and Routes in order to handle http requests and responses.
 * Extending Akka Directives
 * For more information, please read Akka directives documentation:
 * https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/index.html
 */
public class Directives extends AllDirectives {
    private static final Logger log = LoggerFactory.getLogger(Directives.class);

    private final NumbersRoute numbersRoute;
    private final int port;

    public Directives(PrimeNumbersProtocol client, NumbersRoute numbersRoute, int port) {
        this.numbersRoute = new NumbersRoute(client);
        this.port = port;
        runServer();
    }

    /**
     * Returns a route matching resources that should be accessible over HTTP.
     */
    public Route http() {
        Route numRoute = 
            pathPrefix("prime", () -> numbersRoute.numbersRoute());
        
        /**
         * Route for handling get requests
         */
        Route getStatus = get(() -> {
            return path(segment("status"), () -> {
                return complete("Server is running");
            });
        });
        return numRoute.orElse(getStatus);
    }
    /**
     * Run the server, start actor system and bind the port in order to listen to requests
     */
    public void runServer() {
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final Http http = Http.get(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = http().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("0.0.0.0", port), materializer);
        binding.thenAccept(b -> {
            log.debug("Server bound to port {}", b.localAddress());
        });
    }
}
