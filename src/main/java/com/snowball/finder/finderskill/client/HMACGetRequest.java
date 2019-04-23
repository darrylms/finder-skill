package com.snowball.finder.finderskill.client;

import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.utils.URLParamEncoder;
import com.mashape.unirest.request.HttpRequest;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HMACGetRequest extends HttpRequest {

    protected String canonicalUri;
    protected String canonicalQuery;

    public HMACGetRequest(String url) {
        super(HttpMethod.GET, url);
        canonicalUri = url;
        canonicalQuery = "";
    }

    @Override
    public HMACGetRequest routeParam(String name, String value) {
        Matcher matcher = Pattern.compile("\\{" + name + "\\}").matcher(this.url);
        int count;
        for(count = 0; matcher.find(); ++count) {
        }
        if (count == 0) {
            throw new RuntimeException("Can't find route parameter name \"" + name + "\"");
        } else {
            this.url = this.url.replaceAll("\\{" + name + "\\}", URLParamEncoder.encode(value));
            this.canonicalUri = this.canonicalUri.replaceAll("\\{" + name + "\\}", URLParamEncoder.encode(URLParamEncoder.encode(value)));
            return this;
        }
    }

    @Override
    public HMACGetRequest queryString(String name, Collection<?> value) {
        Iterator var3 = value.iterator();
        while(var3.hasNext()) {
            Object cur = var3.next();
            this.queryString(name, cur);
        }

        return this;
    }

    @Override
    public HMACGetRequest queryString(String name, Object value) {
        StringBuilder queryString = new StringBuilder();
        if (this.url.contains("?")) {
            queryString.append("&");
            canonicalQuery = canonicalQuery + "&"; //No leading question marks in canonicalQuery.
        } else {
            queryString.append("?");
        }

        try {
            queryString.append(URLEncoder.encode(name)).append("=").append(URLEncoder.encode(value == null ? "" : value.toString(), "UTF-8"));
            canonicalQuery = canonicalQuery + URLEncoder.encode(URLEncoder.encode(name)) + "=" + URLEncoder.encode(URLEncoder.encode(value == null ? "" : value.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException var5) {
            throw new RuntimeException(var5);
        }

        this.url = this.url + queryString.toString();
        return this;
    }

    @Override
    public HMACGetRequest header(String name, String value) {
        return (HMACGetRequest)super.header(name, value);
    }

    @Override
    public HMACGetRequest headers(Map<String, String> headers) {
        return (HMACGetRequest)super.headers(headers);
    }

    @Override
    public HMACGetRequest basicAuth(String username, String password) {
        super.basicAuth(username, password);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public String getCanonicalUri() throws URISyntaxException {
        URI uri = new URI(canonicalUri);
        return uri.getRawPath();
        //return this.getURI().getRawPath();
    }

    public String getCanonicalQuery() {
        return canonicalQuery;
    }

    public URI getURI() throws URISyntaxException {
        URI uri = new URI(url);
        return uri;
    }
}
