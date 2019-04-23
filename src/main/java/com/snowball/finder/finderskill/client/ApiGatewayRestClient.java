package com.snowball.finder.finderskill.client;

import com.amazonaws.auth.*;
import com.mashape.unirest.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// TODO: Initialise this with a builder
public class ApiGatewayRestClient extends BasicAuthClient {

    static final Logger logger = LoggerFactory.getLogger(ApiGatewayRestClient.class);

    private String accessKeyId;
    private String secretKey;
    private String sessionToken;

    public ApiGatewayRestClient() {
        super();
        EnvironmentVariableCredentialsProvider provider = new EnvironmentVariableCredentialsProvider();
        AWSCredentials cred = provider.getCredentials();
        accessKeyId = cred.getAWSAccessKeyId();
        secretKey = cred.getAWSSecretKey();
        sessionToken = Optional.ofNullable(System.getenv("AWS_SESSION_TOKEN")).orElse("");
    }

    public <T> HttpResponse<T> getWithHMAC(String endpoint, Map<String, String> queryParams, Map<String, String> routeParams, Class<T> clazz) throws Exception {
        URI uri = new URI(endpoint);
        String method = "GET";
        String service = "execute-api";
        String region = Optional.ofNullable(System.getenv("AWS_REGION")).orElse("eu-west-1");
        String algorithm = "AWS4-HMAC-SHA256";
        String contentType = "application/x-www-form-urlencoded"; //Set to "application/x-amz-json-1.0" for PUT, POST or PATCH
        String signedHeader = "content-type;host;x-amz-date;x-amz-security-token"; //Headers must be in alphabetical order
        LocalDateTime now = LocalDateTime.now();
        String amzDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        String datestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        HMACGetRequest request = new HMACGetRequest(endpoint);

        String canonicalHeaders = "content-type:" + contentType + "\n" + "host:" + uri.getHost() + "\n" + "x-amz-date:" + amzDate + "\n" +
                "x-amz-security-token:" + sessionToken + "\n";
        if (accessKeyId.isEmpty() || secretKey.isEmpty()) {
            throw new RuntimeException("IAM credentials missing");
        }
        if(routeParams != null) {
            routeParams.forEach(request::routeParam);
        }
        // TODO: Sort the query params alphabetically before adding
        if (queryParams != null) {
            queryParams.forEach(request::queryString);
        }
        String canonicalRequest = getCanonicalRequest(method, signedHeader, canonicalHeaders, request);
        String authHeader = getAuthorisationHeaderContent(service, region, algorithm, signedHeader, amzDate, datestamp, canonicalRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Host", uri.getHost());
        headers.put("X-Amz-Date", amzDate);
        headers.put("Authorization", authHeader);
        request.headers(headers);

        HttpResponse<T> response = request.asObject(clazz);
        return response;
    }

    protected String getCanonicalRequest(String method, String signedHeader, String canonicalHeaders, HMACGetRequest request) throws URISyntaxException, NoSuchAlgorithmException {
        String canonicalQueryString = request.getCanonicalQuery();
        String canonicalUri = request.getCanonicalUri();
        return method + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n" +
                canonicalHeaders + "\n" + signedHeader + "\n" + getPayloadHash("");
    }

    protected String getAuthorisationHeaderContent(String service, String region, String algorithm, String signedHeader, String amzDate, String datestamp, String canonicalRequest) throws Exception {
        String credentialScope = datestamp + '/' + region + '/' + service + '/' + "aws4_request";
        String stringToSign = algorithm + '\n' +  amzDate + '\n' +  credentialScope + '\n' +  getPayloadHash(canonicalRequest);
        byte[] signingKey = getSignatureKey(secretKey, datestamp, region, service);
        String signature = bytesToHex(HmacSHA256(stringToSign, signingKey));
        return algorithm + " " + "Credential=" + accessKeyId + '/' + credentialScope + ", " +  "SignedHeaders=" + signedHeader + ", " + "Signature=" + signature;
    }

    protected static String getPayloadHash(String payload) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    protected static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF-8");
        byte[] kDate = HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = HmacSHA256(regionName, kDate);
        byte[] kService = HmacSHA256(serviceName, kRegion);
        byte[] kSigning = HmacSHA256("aws4_request", kService);
        return kSigning;
    }

    protected static byte[] HmacSHA256(String data, byte[] key) throws Exception {
        String algorithm="HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF-8"));
    }

    protected static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
