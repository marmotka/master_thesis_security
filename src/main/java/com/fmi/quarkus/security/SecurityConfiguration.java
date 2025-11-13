//package com.fmi.quarkus.security;
//
//import io.quarkus.security.identity.SecurityIdentity;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.enterprise.inject.Produces;
//import jakarta.inject.Inject;
//import jakarta.inject.Named;
//
//@ApplicationScoped
//public class SecurityConfiguration {
//
//    @Produces
//    @Named("currentUser")
//    public SecurityIdentity getCurrentUser(@Inject SecurityIdentity secId) {  // Inject here—no field
//        return secId;
//    }
//
//}