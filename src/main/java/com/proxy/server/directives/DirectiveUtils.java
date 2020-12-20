package com.proxy.server.directives;

import akka.NotUsed;
import akka.http.javadsl.common.EntityStreamingSupport;
import akka.http.javadsl.common.JsonEntityStreamingSupport;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaType;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class DirectiveUtils {
    /**
     * "Accepted", "The request has been accepted for processing, but the processing has not been completed. 
     * */
    public static HttpResponse ACCEPTED = HttpResponse.create().withStatus(StatusCodes.ACCEPTED);
    
    /**
     * "Bad Request", "The request contains bad syntax or cannot be fulfilled.
     */
    public static HttpResponse BAD_REQUEST = HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST);
    
    /**
     *  Returns 404 response with a body including the validation error
     */
    public static HttpResponse withValidationError(Set<String> validationErrors) {
        StringBuilder errors = new StringBuilder();
        validationErrors.forEach(ve -> errors.append(ve));
        return HttpResponse.create()
            .withStatus(StatusCodes.NOT_ACCEPTABLE)
            .withEntity(errors.toString());
    }

    /**
     * Source compact JSON streaming 
     * https://doc.akka.io/docs/akka-http/current/routing-dsl/source-streaming-support.html
     */
    public static JsonEntityStreamingSupport compactJsonSupport() {
        ByteString start = ByteString.fromString("[");
        ByteString between = ByteString.fromString(",");
        ByteString end = ByteString.fromString("]");
        Flow<ByteString, ByteString, NotUsed> compactArrayRendering =
          Flow.of(ByteString.class).intersperse(start, between, end);
        return EntityStreamingSupport.json()
            .withFramingRendererFlow(compactArrayRendering);
    }
    
    /**
     * Source JSON streaming 
     * https://doc.akka.io/docs/akka-http/current/routing-dsl/source-streaming-support.html
     */
    public static JsonEntityStreamingSupport jsonSupport() {
        return EntityStreamingSupport.json();
    }
}
