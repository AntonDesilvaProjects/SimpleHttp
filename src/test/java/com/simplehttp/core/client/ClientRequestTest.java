package com.simplehttp.core.client;

import com.simplehttp.core.client.http.HttpMultiValueMap;
import com.simplehttp.core.annotation.client.SimpleHttpClient;
import com.simplehttp.core.annotation.http.*;
import com.simplehttp.core.client.http.HttpMethod;
import com.simplehttp.core.client.model.Response;
import com.simplehttp.core.exception.RequestInterceptorException;
import com.simplehttp.core.exception.ResponseInterceptorException;
import com.simplehttp.core.exception.SimpleHttpException;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.net.http.HttpTimeoutException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClientRequestTest {

    @Test
    public void unsupportedMethodTest() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.example.com")
        interface TestClient {
            void nonHttpMethod();
        }
        TestClient testClient = new ClientBuilder().buildClient(TestClient.class);
        assertThatThrownBy(testClient::nonHttpMethod)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("The method 'nonHttpMethod' is not decorated for Simple HTTP client request");
    }

    @Test
    public void onlyRootUrlTest() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.example.com")
        interface TestClient { @RequestAttribute void getEntity();}

        HttpClient assertionHttpClient = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getUrl()).isEqualTo("https://www.example.com");
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(assertionHttpClient).buildClient(TestClient.class);
        testClient.getEntity();
    }

    @Test
    public void rootUrlWithPathFragmentFromRequestAttributeTest() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute("/entity")
            void listEntities();
        }

        HttpClient assertionHttpClient = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getUrl()).isEqualTo("https://www.example.com/entity");
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(assertionHttpClient).buildClient(TestClient.class);
        testClient.listEntities();
    }

    @Test
    public void fullUrlFromRequestAttributeTest() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute("https://www.some-other-example.com")
            void listEntities();
        }

        HttpClient assertionHttpClient = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getUrl()).isEqualTo("https://www.some-other-example.com");
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(assertionHttpClient).buildClient(TestClient.class);
        testClient.listEntities();
    }

    @Test
    public void rootUrlWithPathFragmentFromMethodParamTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            void listEntities(@Url String path);
        }

        HttpClient assertionHttpClient = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getUrl()).isEqualTo("https://www.example.com/allEntities");
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(assertionHttpClient).buildClient(TestClient.class);
        testClient.listEntities("allEntities");
    }

    @Test
    public void fullUrlFromMethodParamTest() {
        @SimpleHttpClient(name = "MyClient", host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            void listEntities(@Url String path);
        }

        HttpClient assertionHttpClient = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getUrl()).isEqualTo("https://www.some-other-example.com");
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(assertionHttpClient).buildClient(TestClient.class);
        testClient.listEntities("https://www.some-other-example.com");
    }

    @Test
    public void pathParamValueTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute("/{entityType}/{ entityId }")
            void getEntity(@PathParam("entityType") String type, @PathParam("entityId") int id);
        }

        HttpClient assertionHttpClient = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getUrl()).isEqualTo("https://www.example.com/employee/12");
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(assertionHttpClient).buildClient(TestClient.class);
        testClient.getEntity("employee", 12);
    }

    @Test
    public void missingPathParamValueTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute("/{entityType}/{entityId}")
            void getEntity(@PathParam("entityType") String type, @PathParam("entityId") int id);
        }
        TestClient testClient = new ClientBuilder().buildClient(TestClient.class);
        assertThatThrownBy(() -> testClient.getEntity(null, 12))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The value for path parameter 'entityType' is empty");
    }

    @Test
    public void httpMethodTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            void getEntity();

            @RequestAttribute(httpMethod = HttpMethod.PATCH)
            void getEntities(Object patch);
        }
        HttpClient defaultMethodAssertion = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getHttpMethod()).isEqualTo(HttpMethod.GET);
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(defaultMethodAssertion).buildClient(TestClient.class);
        testClient.getEntity();

        HttpClient providedMethodAssertion = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getHttpMethod()).isEqualTo(HttpMethod.PATCH);
            return Response.builder().build();
        };
        TestClient testClient2 = new ClientBuilder().withHttpClient(providedMethodAssertion).buildClient(TestClient.class);
        testClient2.getEntities(null);
    }

    @Test
    public void headersTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute(
                    headers = {
                        "Content-Type=application/json", "X-Request-Name=getEntity", "Accept=application/json"
                    }
            )
            String getEntity(@HttpHeader("Authorization") String token,
                             @HttpHeader("NullIgnored") String nullIgnored,
                             @HttpHeader Map<String, String> dynamicHeaders,
                             @HttpHeader Map<String, List<String>> dynamicHeaderList);
        }
        HttpClient httpClientAssertion = request -> {
            HttpMultiValueMap expectedHeaders = new HttpMultiValueMap();
            expectedHeaders.put("Content-Type", List.of("application/json"));
            expectedHeaders.put("X-Request-Name", List.of("getEntity"));
            expectedHeaders.put("Accept", List.of("application/json", "application/cbor"));
            expectedHeaders.put("Authorization", List.of("Bearer token"));
            expectedHeaders.put("Test", List.of("test1", "test2", "test3"));
            expectedHeaders.put("X-Auth-Type", List.of("bearer", "basic", "oauth"));

            assertThat(request).isNotNull();
            assertThat(request.getHeaders()).isEqualTo(expectedHeaders);
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(httpClientAssertion).buildClient(TestClient.class);
        testClient.getEntity(
                "Bearer token",
                null,
                Map.of("Accept", "application/cbor", "Test", "test1"),
                Map.of("X-Auth-Type", List.of("bearer", "basic", "oauth"), "Test", List.of("test2", "test3")));
    }

    @Test
    public void queryParamsTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute(
                    queryParams = {
                            "Content-Type=application/json", "X-Request-Name=getEntity", "Accept=application/json"
                    }
            )
            String getEntity(@QueryParam("Authorization") String token,
                             @QueryParam("NullIgnored") String nullIgnored,
                             @QueryParam Map<String, String> dynamicHeaders,
                             @QueryParam Map<String, List<String>> dynamicHeaderList);
        }
        HttpClient httpClientAssertion = request -> {
            HttpMultiValueMap expectedQueryParams = new HttpMultiValueMap();
            expectedQueryParams.put("Content-Type", List.of("application/json"));
            expectedQueryParams.put("X-Request-Name", List.of("getEntity"));
            expectedQueryParams.put("Accept", List.of("application/json", "application/cbor"));
            expectedQueryParams.put("Authorization", List.of("Bearer token"));
            expectedQueryParams.put("Test", List.of("test1", "test2", "test3"));
            expectedQueryParams.put("X-Auth-Type", List.of("bearer", "basic", "oauth"));

            assertThat(request).isNotNull();
            assertThat(request.getQueryParams()).isEqualTo(expectedQueryParams);
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(httpClientAssertion).buildClient(TestClient.class);
        testClient.getEntity(
                "Bearer token",
                null,
                Map.of("Accept", "application/cbor", "Test", "test1"),
                Map.of("X-Auth-Type", List.of("bearer", "basic", "oauth"), "Test", List.of("test2", "test3")));
    }

    @Test
    public void simpleResponseTypeTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            Response getEntity();
        }
        HttpClient httpClientAssertion = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getResponseType()).isEqualTo(Response.class);
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(httpClientAssertion).buildClient(TestClient.class);
        testClient.getEntity();
    }

    @Test
    public void genericResponseTypeTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            Map<String, List<Response>> getEntity();
        }
        HttpClient httpClientAssertion = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getResponseType()).isInstanceOf(ParameterizedType.class);
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(httpClientAssertion).buildClient(TestClient.class);
        testClient.getEntity();
    }

    @Test
    public void noResponseTypeTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute(httpMethod = HttpMethod.DELETE, url = "/{id}")
            void deleteEntity(@PathParam("id") long id);
        }
        HttpClient httpClientAssertion = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getResponseType().getTypeName()).isEqualTo("void");
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(httpClientAssertion).buildClient(TestClient.class);
        testClient.deleteEntity(123);
    }

    @Test
    public void requestBodyTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            String getEntity(@RequestBody Map<String, Object> body);
        }
        HttpClient httpClientAssertion = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getBody()).isEqualTo(Map.of("test", 123));
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder().withHttpClient(httpClientAssertion).buildClient(TestClient.class);
        testClient.getEntity(Map.of("test", 123));
    }

    @Test
    public void requestInterceptorTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            String getEntity();
        }
        HttpClient httpClientAssertion = request -> {
            assertThat(request).isNotNull();
            assertThat(request.getUrl()).isEqualTo("https://www.example.com/path1/path2");
            return Response.builder().build();
        };
        TestClient testClient = new ClientBuilder()
                .withRequestInterceptor(((request, client) -> {
                    request.setUrl(request.getUrl() + "/path1");
                    return request;
                }))
                .withRequestInterceptor(((request, client) -> {
                    request.setUrl(request.getUrl() + "/path2");
                    return request;
                }))
                .withHttpClient(httpClientAssertion)
                .buildClient(TestClient.class);
        testClient.getEntity();
    }

    @Test
    public void requestInterceptorErrorTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            String getEntity();
        }
        TestClient testClient = new ClientBuilder()
                .withRequestInterceptor(((request, client) -> {
                    throw new RuntimeException("Some error during request interceptor");
                })).buildClient(TestClient.class);

        assertThatThrownBy(testClient::getEntity)
                .isInstanceOf(RequestInterceptorException.class)
                .hasMessageContaining("Some error during request interceptor");
    }

    @Test
    public void responseInterceptorTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            String uppercase();

            @RequestAttribute
            String lowercase();
        }
        HttpClient restHttpClient = request -> Response.builder().parsedResponse("Hello, World!").build();
        TestClient testClient = new ClientBuilder()
                .withResponseInterceptor((response, request, client) -> {
                    String methodName = request.getMethod().getName();
                    String responseBody = (String) response.getParsedResponse();
                    if ("uppercase".equals(methodName)) {
                        response.setParsedResponse(responseBody.toUpperCase(Locale.ROOT));
                    } else {
                        response.setParsedResponse(responseBody.toLowerCase(Locale.ROOT));
                    }
                    return response;
                })
                .withHttpClient(restHttpClient)
                .buildClient(TestClient.class);

        assertThat(testClient.uppercase()).isEqualTo("HELLO, WORLD!");
        assertThat(testClient.lowercase()).isEqualTo("hello, world!");
    }

    @Test
    public void responseInterceptorErrorTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            String uppercase();
        }
        HttpClient restHttpClient = request -> Response.builder().parsedResponse("Hello, World!").build();
        TestClient testClient = new ClientBuilder()
                .withResponseInterceptor((response, request, client) -> {
                    throw new NullPointerException("Some Null Value");
                })
                .withHttpClient(restHttpClient)
                .buildClient(TestClient.class);

        assertThatThrownBy(testClient::uppercase)
                .isInstanceOf(ResponseInterceptorException.class)
                .hasMessageContaining("Some Null Value");
    }

    @Test
    public void errorHandlerTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            String getEntity();
        }
        HttpClient restHttpClient = request -> { throw new HttpTimeoutException("Request timed out!"); };
        AtomicReference<String> logger = new AtomicReference<>();
        TestClient testClient = new ClientBuilder()
                .withErrorHandler((request, response, e) -> {
                    logger.set("Some Error: " + e.getLocalizedMessage());
                    return null;
                })
                .withErrorHandler((request, response, e) -> response == null ? Response.builder().build() : response)
                .withErrorHandler(((request, response, exception) -> {
                    if (response != null && response.getParsedResponse() == null) {
                        response.setParsedResponse("fallback value");
                    }
                    return response;
                }))
                .withHttpClient(restHttpClient)
                .buildClient(TestClient.class);

        assertThat(testClient.getEntity()).isEqualTo("fallback value");
        assertThat(logger.get()).isEqualTo("Some Error: Request timed out!");
    }

    @Test
    public void noErrorHandlerTest() {
        @SimpleHttpClient(host = "https://www.example.com")
        interface TestClient {
            @RequestAttribute
            String getEntity();
        }
        HttpClient restHttpClient = request -> { throw new HttpTimeoutException("Request timed out!"); };
        TestClient testClient = new ClientBuilder()
                .withHttpClient(restHttpClient)
                .buildClient(TestClient.class);

        assertThatThrownBy(testClient::getEntity).isInstanceOf(SimpleHttpException.class)
                .hasMessage("Error while executing request [GET] https://www.example.com");
    }


}
