package com.snowball.finder.finderskill.client;

import com.amazonaws.auth.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class BasicAuthClient {

    static final Logger logger = LoggerFactory.getLogger(BasicAuthClient.class);

    public BasicAuthClient() {
        Unirest.setObjectMapper(new ObjectMapper() {
            //Ensure Jackson modules to handle Java 8 datatypes are loaded.
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper().registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).findAndRegisterModules();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public <T> HttpResponse<T> get(String endpoint, Map<String, String> queryParams, Map<String, String> routeParams, Class<T> clazz) throws UnirestException {
        return get(endpoint, queryParams, routeParams, null, clazz);
    }

    public <T> HttpResponse<T> get(String endpoint, Map<String, String> queryParams, Map<String, String> routeParams, Map<String, String> headers, Class<T> clazz) throws UnirestException {
        GetRequest request = Unirest.get(endpoint);
        constructGetRequest(queryParams, routeParams, headers, request);
        return request.asObject(clazz);
    }

    public <T> HttpResponse<T> post(String endpoint, Map<String, Object> fields, Map<String, String> routeParams, Class<T> clazz) throws UnirestException {
        return post(endpoint, fields, routeParams, null, clazz);
    }

    public <T> HttpResponse<T> post(String endpoint, Map<String, Object> fields, Map<String, String> routeParams, Map<String, String> headers, Class<T> clazz) throws UnirestException {
        HttpRequestWithBody request = Unirest.post(endpoint);
        constructPostRequest(fields, routeParams, headers, request);
        return request.asObject(clazz);
    }

    public <T> HttpResponse<T> post(String endpoint, Object body, Map<String, String> routeParams, Map<String, String> headers, Class<T> clazz) throws UnirestException {
        HttpRequestWithBody request = Unirest.post(endpoint);
        constructPostRequest(null, routeParams, headers, request);
        request.body(body);
        return request.asObject(clazz);
    }

    public <T> HttpResponse<T> getWithBasicAuth(String endpoint, Map<String, String> queryParams, Map<String, String> routeParams, Map<String, String> headers, String username, String password, Class<T> clazz) throws UnirestException {
        GetRequest request = Unirest.get(endpoint);
        constructGetRequest(queryParams, routeParams, headers, request);
        request.basicAuth(username, password);
        HttpResponse<T> response = request.asObject(clazz);
        return response;
    }

    public <T> HttpResponse<T> getWithBasicAuth(String endpoint, Map<String, String> queryParams, Map<String, String> routeParams, String username, String password, Class<T> clazz) throws UnirestException {
        return getWithBasicAuth(endpoint, queryParams, routeParams, null, username, password, clazz);
    }

    private void constructGetRequest(Map<String, String> queryParams, Map<String, String> routeParams, Map<String, String> headers, GetRequest request) {
        if (routeParams != null) routeParams.forEach(request::routeParam);
        if (queryParams != null) queryParams.forEach(request::queryString);
        if (headers != null) request.headers(headers);
    }

    private void constructPostRequest(Map<String, Object> fields, Map<String, String> routeParams, Map<String, String> headers, HttpRequestWithBody request) {
        if (routeParams != null) routeParams.forEach(request::routeParam);
        if (fields != null) request.fields(fields);
        if (headers != null) request.headers(headers);
    }

}
