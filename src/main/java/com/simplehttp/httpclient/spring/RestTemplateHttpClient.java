package com.simplehttp.httpclient.spring;

import com.simplehttp.core.client.http.HttpMultiValueMap;
import com.simplehttp.core.client.HttpClient;
import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.model.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

public class RestTemplateHttpClient implements HttpClient {

    private final RestTemplate restTemplate;

    public RestTemplateHttpClient() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Response execute(Request request) {
        final String url = getUrl(request.getUrl(), request.getQueryParams());
        final HttpMethod method = Optional.ofNullable(HttpMethod.resolve(request.getHttpMethod().name()))
                .orElse(HttpMethod.GET);
        final HttpHeaders headers = getHeaders(request.getHeaders());
        final Object body = request.getBody();

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        ResponseEntity<?> response = restTemplate.exchange(url, method, entity,
                ParameterizedTypeReference.forType(request.getResponseType()));

        return Response.builder()
                .parsedResponse(response.getBody())
                .build();
    }

    private HttpHeaders getHeaders(HttpMultiValueMap headers) {
        final HttpHeaders springHeaders = new HttpHeaders();
        for (String key: headers.keySet()) {
            final List<String> headerValues = headers.getValues(key);
            if (!CollectionUtils.isEmpty(headerValues)) {
                springHeaders.put(key, headerValues);
            }
        }
        return springHeaders;
    }

    private String getUrl(String url, HttpMultiValueMap queryParams) {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        for (String key: queryParams.keySet()) {
            for (String value: queryParams.getValues(key)) {
                builder.queryParam(key, value);
            }
        }
        return builder.toUriString();
    }

    public static void main(String... args) {
        Request request = Request.builder()
                .url("https://webhook.site/67835d52-2f92-4a1a-a1b2-667ad708c0a4")
                .httpMethod(com.simplehttp.core.client.http.HttpMethod.GET)
                .headers(new HttpMultiValueMap())
                .queryParams(new HttpMultiValueMap())
                .build();
        RestTemplateHttpClient client = new RestTemplateHttpClient();
        Response response = client.execute(request);
        System.out.println(response);
    }
}
