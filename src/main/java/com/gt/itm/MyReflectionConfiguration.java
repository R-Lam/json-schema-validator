package com.gt.itm;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {org.apache.camel.component.jsonvalidator.JsonValidationException.class})
public class MyReflectionConfiguration {
    
}
