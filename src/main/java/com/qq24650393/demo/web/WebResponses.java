package com.qq24650393.demo.web;

final class WebResponses {

    private WebResponses() {
    }

    static <T> T success(T response) {
        set(response, "setCode", "0");
        set(response, "setMessage", "success");
        return response;
    }

    private static void set(Object target, String methodName, String value) {
        try {
            target.getClass().getMethod(methodName, String.class).invoke(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Generated response model is missing " + methodName, ex);
        }
    }
}
