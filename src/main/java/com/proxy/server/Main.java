package com.proxy.server;

import com.primenumbers.server.grpc.PrimeNumbersServiceClient;
import com.proxy.server.directives.Directives;
import com.proxy.server.directives.NumbersRoute;
import com.proxy.server.primenumbersserver.PrimeNumbersProtocol;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

public class Main {
    public static void main(String[] args) throws Exception {

        Config config = ConfigFactory.load().getConfig("server");
        
        ActorSystem system = ActorSystem.create("Proxy-Server");
        Materializer materializer = ActorMaterializer.create(system);

        // Configure the client by code:
        GrpcClientSettings settings = GrpcClientSettings.connectToServiceAt(
            config.getString("prime_numbers_server_ip"), config.getInt("prime_numbers_server_port"), system
        ).withTls(false);

        PrimeNumbersServiceClient client = PrimeNumbersServiceClient.create(settings, system);
        PrimeNumbersProtocol protocol = new PrimeNumbersProtocol(materializer, client);
        NumbersRoute numbersRoute = new NumbersRoute(protocol);
        
        // Bind the proxy server and run it
        new Directives(protocol, numbersRoute , config.getInt("port"));
    }
}
