package com.fmi.quarkus.web;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

public class Templates {

    @CheckedTemplate
    public static class Pages {
        public static native TemplateInstance login(String message, String errorMessage);
        public static native TemplateInstance register(Object user, String errorMessage, String successMessage);
        public static native TemplateInstance tasks(java.util.List<?> tasks, String message, String messageType);
        public static native TemplateInstance profile(String currentUsername, String currentEmail, Object passwordForm, Boolean openPassword);
        public static native TemplateInstance admin_users(java.util.List<?> users, String currentUsername, String successMessage, String errorMessage);
    }

}