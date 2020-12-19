package com.proxy.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primenumbers.server.grpc.PrimeNumbersServiceClient;
import com.proxy.server.primenumbersserver.PrimeNumbersProtocol;

import akka.actor.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        String serverHost = "127.0.0.1";
        int serverPort = 8090;

        ActorSystem system = ActorSystem.create("Proxy-Server");
        Materializer materializer = ActorMaterializer.create(system);

        // Configure the client by code:
        GrpcClientSettings settings = GrpcClientSettings.connectToServiceAt(serverHost, serverPort, system).withTls(false);

        // Or via application.conf:
        // GrpcClientSettings settings = GrpcClientSettings.fromConfig(GreeterService.name, system);

        PrimeNumbersServiceClient client = PrimeNumbersServiceClient.create(settings, system);
        PrimeNumbersProtocol protocol = new PrimeNumbersProtocol(materializer, client);
    }
}
