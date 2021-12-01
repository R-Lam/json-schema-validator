package com.gt.itm;

import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.http.base.HttpOperationFailedException;

public class CamelErrorProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        HttpOperationFailedException e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
        exchange.getMessage().setBody(e.getResponseBody());
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, e.getStatusCode());
    }
    
}
