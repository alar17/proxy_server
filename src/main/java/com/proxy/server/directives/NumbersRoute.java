package com.proxy.server.directives;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proxy.server.primenumbersserver.PrimeNumbersProtocol;

import akka.NotUsed;
import akka.http.javadsl.common.EntityStreamingSupport;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import io.vavr.Tuple2;
import io.vavr.control.Option;

/**
 * Directive class which deals with Model1 routes
 */
public class NumbersRoute extends AllDirectives {
    private static final Logger log = LoggerFactory.getLogger(NumbersRoute.class);
    private final PrimeNumbersProtocol client;
    
    public NumbersRoute(PrimeNumbersProtocol client) {
        this.client = client;
    }
    
    public Route numbersRoute() {
        return path(PathMatchers.integerSegment(), number -> 
            get(() -> {
                log.info("Received get request for Number: {}", number);
                
                Tuple2<Option<String>, Source<Integer, NotUsed>> response = client.readItem(number);
                if(response._1().isDefined()) {
                    return complete(response._1().get().equals(DirectiveUtils.SERVER_OVERLOADED_MESSAGE) ?
                        DirectiveUtils.SERVER_OVERLOADED :
                        DirectiveUtils.BAD_REQUEST);
                } else {
                    Source<ByteString, NotUsed> map = response._2().map(i -> ByteString.fromString(i.toString()));
                    return completeOKWithSource(map, Marshaller.byteStringMarshaller(ContentTypes.APPLICATION_JSON), EntityStreamingSupport.json()
                        .withContentType(ContentTypes.APPLICATION_JSON)
                        .withParallelMarshalling(10, false));
                }
            })
        );
    }    
}
