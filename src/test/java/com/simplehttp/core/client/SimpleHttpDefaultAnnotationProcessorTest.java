package com.simplehttp.core.client;

import com.simplehttp.core.Constants;
import com.simplehttp.core.client.http.HttpMultiValueMap;
import com.simplehttp.core.TypeReference;
import com.simplehttp.core.annotation.SimpleHttpDefaultAnnotationProcessor;
import com.simplehttp.core.annotation.client.SimpleHttpClient;
import com.simplehttp.core.annotation.http.*;
import com.simplehttp.core.client.http.HttpMethod;
import com.simplehttp.core.client.model.ClientMetadata;
import com.simplehttp.core.client.model.ClientMethodMetaData;
import com.simplehttp.core.client.model.NamedParameterMetaData;
import com.simplehttp.core.client.model.ParameterMetaData;
import com.simplehttp.httpclient.spring.Person;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SimpleHttpDefaultAnnotationProcessorTest {

    private final SimpleHttpDefaultAnnotationProcessor simpleHttpDefaultAnnotationProcessor = new SimpleHttpDefaultAnnotationProcessor();

    @Test
    public void nonSimpleHttpClient() {
        interface BadClient {}
        assertThatThrownBy(() -> simpleHttpDefaultAnnotationProcessor.extractClientMetadata(BadClient.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target class not annotated for Simple HTTP client");
    }

    @Test
    public void simpleHttpClientAnnotationDefaultValues() {
        @SimpleHttpClient
        interface TestClient {}

        ClientMetadata metadata = simpleHttpDefaultAnnotationProcessor.extractClientMetadata(TestClient.class);
        assertThat(metadata).isNotNull();
        assertThat(metadata.getName()).isEqualTo(TestClient.class.getName());
        assertThat(metadata.getHost()).isEmpty();
    }

    @Test
    public void simpleHttpClientAnnotationProvidedValues() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.example.com")
        interface TestClient {}

        ClientMetadata metadata = simpleHttpDefaultAnnotationProcessor.extractClientMetadata(TestClient.class);
        assertThat(metadata).isNotNull();
        assertThat(metadata.getName()).isEqualTo("MyClient");
        assertThat(metadata.getHost()).isEqualTo("https://www.example.com");
    }

    @Test
    public void requestMethodAnnotationDefaultValues() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.example.com")
        interface TestClient {
            void nonAnnotatedMethod(); // this method should be ignored

            @RequestAttribute
            String getEntity();
        }

        ClientMetadata metadata = simpleHttpDefaultAnnotationProcessor.extractClientMetadata(TestClient.class);
        assertThat(metadata).isNotNull();
        assertThat(metadata.getMethodNameToRequestTemplate()).hasSize(1);

        ClientMethodMetaData methodMetaData = metadata.getMethodNameToRequestTemplate().get("getEntity");
        assertThat(methodMetaData).isNotNull();
        assertThat(methodMetaData.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(methodMetaData.getUrl()).isEqualTo(Constants.DEFAULT_STRING_VALUE);
        assertThat(methodMetaData.getResponseType()).isEqualTo(String.class);
        assertThat(methodMetaData.getHeaders()).isEqualTo(new HttpMultiValueMap());
        assertThat(methodMetaData.getQueryParams()).isEqualTo(new HttpMultiValueMap());
    }

    @Test
    public void requestMethodAnnotationProvidedValues() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.entity-service.io")
        interface TestClient {
            @RequestAttribute(
                    httpMethod = HttpMethod.PATCH,
                    url = "/entities",
                    queryParams = {"page=1", "pageSize=250", "sort=name"},
                    headers = {"Authorization=Bearer token", "region=sgp", "region=esp"})
            List<String> getEntities();
        }

        ClientMetadata metadata = simpleHttpDefaultAnnotationProcessor.extractClientMetadata(TestClient.class);
        assertThat(metadata).isNotNull();
        assertThat(metadata.getMethodNameToRequestTemplate()).hasSize(1);

        ClientMethodMetaData methodMetaData = metadata.getMethodNameToRequestTemplate().get("getEntities");
        assertThat(methodMetaData).isNotNull();
        assertThat(methodMetaData.getHttpMethod()).isEqualTo(HttpMethod.PATCH);
        assertThat(methodMetaData.getUrl()).isEqualTo("/entities");
        assertThat(methodMetaData.getResponseType()).isEqualTo(new TypeReference<List<String>>(){}.getType());
        HttpMultiValueMap expectedHeaders = new HttpMultiValueMap();
        expectedHeaders.put("Authorization", List.of("Bearer token"));
        expectedHeaders.put("region", List.of("sgp", "esp"));
        assertThat(methodMetaData.getHeaders()).isEqualTo(expectedHeaders);

        HttpMultiValueMap expectedQueryParams = new HttpMultiValueMap();
        expectedQueryParams.put("page", List.of("1"));
        expectedQueryParams.put("pageSize", List.of("250"));
        expectedQueryParams.put("sort", List.of("name"));
        assertThat(methodMetaData.getQueryParams()).isEqualTo(expectedQueryParams);
    }

    @Test
    public void requestMethodParameterMetadata() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.entity-service.io")
        interface TestClient {
            @RequestAttribute(httpMethod = HttpMethod.POST)
            List<Person> getEntities(@HttpHeader("Authorization") String token,
                                     @HttpHeader Map<String, String> dynamicHeaders,
                                     @QueryParam("page") String page,
                                     @QueryParam Map<String, String> dynamicQueryParams,
                                     @PathParam("accountId") String accountId,
                                     @RequestBody List<String> payload,
                                     @Url String url,
                                     Object notUsed);
        }

        ClientMetadata metadata = simpleHttpDefaultAnnotationProcessor.extractClientMetadata(TestClient.class);
        assertThat(metadata).isNotNull();
        assertThat(metadata.getMethodNameToRequestTemplate()).hasSize(1);

        ClientMethodMetaData methodMetaData = metadata.getMethodNameToRequestTemplate().get("getEntities");
        assertThat(methodMetaData).isNotNull();
        List<ParameterMetaData> parameterMetaData = methodMetaData.getParameterMetaDataList();
        assertThat(parameterMetaData).isNotEmpty().hasSize(8);

        assertThat(parameterMetaData.get(0)).isInstanceOf(NamedParameterMetaData.class);
        NamedParameterMetaData header = (NamedParameterMetaData) parameterMetaData.get(0);
        assertThat(header.getType()).isEqualTo(ParameterMetaData.Type.HTTP_HEADER);
        assertThat(header.getName()).isEqualTo("Authorization");

        assertThat(parameterMetaData.get(1)).isNotNull();
        assertThat(parameterMetaData.get(1).getType()).isEqualTo(ParameterMetaData.Type.HTTP_HEADER_MAP);

        assertThat(parameterMetaData.get(2)).isInstanceOf(NamedParameterMetaData.class);
        NamedParameterMetaData queryParam = (NamedParameterMetaData) parameterMetaData.get(2);
        assertThat(queryParam.getType()).isEqualTo(ParameterMetaData.Type.QUERY_PARAM);
        assertThat(queryParam.getName()).isEqualTo("page");

        assertThat(parameterMetaData.get(3)).isNotNull();
        assertThat(parameterMetaData.get(3).getType()).isEqualTo(ParameterMetaData.Type.QUERY_PARAM_MAP);

        assertThat(parameterMetaData.get(4)).isInstanceOf(NamedParameterMetaData.class);
        NamedParameterMetaData pathParam = (NamedParameterMetaData) parameterMetaData.get(4);
        assertThat(pathParam.getType()).isEqualTo(ParameterMetaData.Type.PATH_PARAM);
        assertThat(pathParam.getName()).isEqualTo("accountId");

        assertThat(parameterMetaData.get(5)).isNotNull();
        assertThat(parameterMetaData.get(5).getType()).isEqualTo(ParameterMetaData.Type.REQUEST_BODY);

        assertThat(parameterMetaData.get(6)).isNotNull();
        assertThat(parameterMetaData.get(6).getType()).isEqualTo(ParameterMetaData.Type.URL);

        assertThat(parameterMetaData.get(7)).isNotNull();
        assertThat(parameterMetaData.get(7).getType()).isEqualTo(ParameterMetaData.Type.NONE);
    }

    @Test
    public void invalidNamedParameter() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.entity-service.io")
        interface TestClient1 {
            @RequestAttribute(httpMethod = HttpMethod.POST)
            List<Person> getEntities(@HttpHeader String token);
        }

        assertThatThrownBy(() -> simpleHttpDefaultAnnotationProcessor.extractClientMetadata(TestClient1.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unnamed header at parameter getEntities#token");

        @SimpleHttpClient(name = "MyClient2", host = "https://www.entity-service.io")
        interface TestClient2 {
            @RequestAttribute
            List<Person> getEntities(@QueryParam String pageSize);
        }
        assertThatThrownBy(() -> simpleHttpDefaultAnnotationProcessor.extractClientMetadata(TestClient2.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unnamed query parameter at parameter getEntities#pageSize");
    }

    @Test
    public void invalidNameValuePair() {
        @SimpleHttpClient(host = "https://www.entity-service.io")
        interface TestClient {
            @RequestAttribute(headers = {"Authorization"})
            List<Person> getEntities();
        }
        assertThatThrownBy(() -> simpleHttpDefaultAnnotationProcessor.extractClientMetadata(TestClient.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid name-value pair at method getEntities: 'Authorization'");
    }
}