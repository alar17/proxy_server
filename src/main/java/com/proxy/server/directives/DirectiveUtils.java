package com.proxy.server.directives;

import akka.NotUsed;
import akka.http.javadsl.common.EntityStreamingSupport;
import akka.http.javadsl.common.JsonEntityStreamingSupport;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import io.vavr.collection.Set;

public class DirectiveUtils {
    public static String SERVER_OVERLOADED_MESSAGE = "Sorry! something went wrong. Please try later";
    public static String INVALID_NUMBERS_MESSAGE = "The prime numbers are not defined for numbers lower than 2";
    public static String INVALID_INPUT_MESSAGE = "The input should be an integer number between 2 and 2147483647";
    
    /**
     * "Bad Request", "The request contains bad syntax or cannot be fulfilled.
     */
    public static HttpResponse BAD_REQUEST = HttpResponse.create()
        .withStatus(StatusCodes.BAD_REQUEST)
        .withEntity(INVALID_INPUT_MESSAGE);
    
    /**
     * "Bad Request", "The request contains bad syntax or cannot be fulfilled.
     */
    public static HttpResponse SERVER_OVERLOADED = HttpResponse.create()
        .withStatus(StatusCodes.INTERNAL_SERVER_ERROR)
        .withEntity(SERVER_OVERLOADED_MESSAGE);
    
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
