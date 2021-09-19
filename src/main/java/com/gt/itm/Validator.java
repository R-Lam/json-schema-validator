package com.gt.itm;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.component.jsonvalidator.JsonValidationException;
import org.apache.camel.support.jsse.*;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Validator extends RouteBuilder {

    private static final String REST_ENDPOINT_ECHO= "{{rest.endpoint}}?bridgeEndpoint=true" +
            "&copyHeaders=true" +
            "&connectionClose=true";

    @Override
    public void configure() throws Exception {
        // SSL Configuration, uncomment only if needed (configured via Secret)
        /* KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource("/deployments/jks/server.jks");
        ksp.setPassword("${JKS_PASSWORD}");
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyPassword("${JKS_PASSWORD}");
        kmp.setKeyStore(ksp);
        KeyStoreParameters tsp = new KeyStoreParameters();
        tsp.setResource("/deployments/jks/truststore.jks");
        tsp.setPassword("${JKS_PASSWORD}");
        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(tsp);
        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setKeyManagers(kmp);
        sslContextParameters.setTrustManagers(tmp);
    
        this.getContext().getRegistry().bind("sslContextParameters", sslContextParameters); */

        onException(JsonValidationException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
            .setHeader(Exchange.CONTENT_TYPE, simple("text/plain"))
            .log("Body: ${body}, Header: ${headers}")
            .setBody(simple("Error: Invalid JSON Schema"));
        
        from("direct:echoServiceUrl")
        .choice()
            .when().simple("'{{custom.header.name}}' != 'null'")
                .setHeader("{{custom.header.name}}", simple("{{custom.header.content}}"))
                    // .to("json-validator:file:/deployments/json-schema/schema.json")
                    .to("json-validator:file:/deployments/schema.json")
                    .choice()
                        .when().simple("'{{rest.endpoint.protocol}}' == 'http'")
                            .to("http://" + REST_ENDPOINT_ECHO)
                                .log("HTTP Response: " + "${body}")
                                .convertBodyTo(String.class)
                                .setBody(simple("${body}"))
                        // Uncomment to use https if needed
                        /* .otherwise()
                            .to("https://" + REST_ENDPOINT_ECHO + "&sslContextParameters=#sslContextParameters")
                                .log("HTTPS Response: " + "${body}")
                                .convertBodyTo(String.class) */
                    .endChoice()
            .otherwise()
                .to("json-validator:file:/deployments/schema.json")
                    .choice()
                        .when().simple("'{{rest.endpoint.protocol}}' == 'http'")
                            .to("http://" + REST_ENDPOINT_ECHO)
                                .log("HTTP Response: " + "${body}")
                                .convertBodyTo(String.class)
                                .setBody(simple("${body}"))
                        // Uncomment to use https if needed
                        /* .otherwise()
                            .to("https://" + REST_ENDPOINT_ECHO + "&sslContextParameters=#sslContextParameters")
                                .log("HTTPS Response: " + "${body}")
                                .convertBodyTo(String.class) */
        .end();
        
        rest()
            .post("/{{rest.endpoint.subpath}}").produces("application/json").enableCORS(true).route()
            .to("direct:echoServiceUrl")
            .endRest();
    }
}