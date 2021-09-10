package com.gt.itm;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.PropertyInject;
import org.apache.camel.Exchange;
import org.apache.camel.component.jsonvalidator.JsonValidationException;

public class Validator extends RouteBuilder {

    @PropertyInject("ENDPOINT")
    private String ENDPOINT;

    @PropertyInject("SCHEMA-FILE")
    private String SCHEMA;

    private static final String REST_ENDPOINT_ECHO= "{{ENDPOINT}}?httpClient.connectTimeout=1000" +
            "&bridgeEndpoint=true" +
            "&copyHeaders=true" +
            "&connectionClose=true";

    @Override
    public void configure() throws Exception {

        onException(JsonValidationException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
            .setHeader(Exchange.CONTENT_TYPE, simple("text/plain"))
            .log("Body: ${body}")
            .setBody(simple("Error: Invalid JSON Schema"));
        
        from("direct:echoServiceUrl")
            .to("json-validator:file:{{SCHEMA}}")
            .to("https://" + REST_ENDPOINT_ECHO)
                .log("Response: " + "${body}")
                .convertBodyTo(String.class)
        .end();
        
        rest()
            .post("/echo").enableCORS(true).route()
            .to("direct:echoServiceUrl");
    }
}