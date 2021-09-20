package com.gt.itm;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {org.apache.camel.component.jsonvalidator.JsonValidationException.class, org.apache.camel.http.base.HttpOperationFailedException.class})
public class MyReflectionConfiguration {
    
}
