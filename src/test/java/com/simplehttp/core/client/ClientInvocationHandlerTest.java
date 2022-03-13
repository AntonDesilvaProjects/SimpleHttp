package com.simplehttp.core.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.simplehttp.core.annotation.client.SimpleHttpClient;
import com.simplehttp.core.annotation.http.PathParam;
import com.simplehttp.core.annotation.http.RequestAttribute;
import com.simplehttp.core.annotation.http.RequestBody;
import com.simplehttp.core.client.http.HttpMethod;
import com.simplehttp.core.client.model.Person;
import com.simplehttp.core.client.model.Response;
import com.simplehttp.core.exception.SimpleHttpException;
import com.simplehttp.httpclient.spring.RestTemplateHttpClient;
import lombok.Data;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class ClientInvocationHandlerTest {

    @Rule
    public WireMockRule wm = new WireMockRule(wireMockConfig().port(3003));

    @Test
    public void test() {
        //configureFor("localhost", 8080);
        stubFor(get("/test").willReturn(okJson("[\n" +
                "\n" +
                "  {\n" +
                "\n" +
                "    \"name\": \"Anton\"\n" +
                "\n" +
                "  },\n" +
                "\n" +
                "  {\n" +
                "\n" +
                "    \"name\": \"Reeka\"\n" +
                "\n" +
                "  }\n" +
                "\n" +
                "]")));

        @SimpleHttpClient(name = "MyClient", host = "http://localhost:3003")
        interface TestClient {
            @RequestAttribute(httpMethod = HttpMethod.GET, url = "/test")
            List<Person> getEntities();
        }

        TestClient client = new ClientBuilder().withHttpClient(new RestTemplateHttpClient())
                .buildClient(TestClient.class);
        List<Person> personList = client.getEntities();
        assertThat(personList).isNotNull();
    }

    @Test
    public void test3() {
        //configureFor("localhost", 8080);
        stubFor(get("/test").willReturn(okJson("[\n" +
                "\n" +
                "  {\n" +
                "\n" +
                "    \"name\": \"Anton\"\n" +
                "\n" +
                "  },\n" +
                "\n" +
                "  {\n" +
                "\n" +
                "    \"name\": \"Reeka\"\n" +
                "\n" +
                "  }\n" +
                "\n" +
                "]")));

        @SimpleHttpClient(name = "MyClient", host = "http://localhost:3003")
        interface TestClient {
            @RequestAttribute(httpMethod = HttpMethod.GET, url = "/test")
            List<Person> getEntities();
        }

        TestClient client = new ClientBuilder().withHttpClient(new RestTemplateHttpClient())
                .buildClient(TestClient.class);
        List<Person> personList = client.getEntities();
        assertThat(personList).isNotNull();
    }

    @Test
    @Ignore
    public void crudTest() {
        @SimpleHttpClient(host = "https://crudcrud.com/api/1295f14b76a74ec08e94eda42c7c80a0/person")
        interface PeopleRepository {
            @RequestAttribute(httpMethod = HttpMethod.POST, headers = {"Content-Type=application/json"})
            Person create(@RequestBody Person person);

            @RequestAttribute("/{id}")
            Person get(@PathParam("id") String id);

            @RequestAttribute
            List<Person> list();

            @RequestAttribute(httpMethod = HttpMethod.PUT, value = "/{id}")
            Person update(@PathParam("id") String id, @RequestBody Person person);

            @RequestAttribute(httpMethod = HttpMethod.DELETE, value = "/{id}")
            void delete(@PathParam("id") String id);
        }

        ClientBuilder clientBuilder = new ClientBuilder();
        PeopleRepository peopleRepository = clientBuilder
                .withRequestInterceptor(((request, client) -> {
                    System.out.println("Request: " + request);
                    return request;
                }))
                .withResponseInterceptor((response, request, client) -> {
                    System.out.println("Response: " + response);
                    return response;
                })
                .withErrorHandler((request, response, exception) -> {
                    if (exception instanceof HttpClientErrorException) {
                        HttpClientErrorException errorException = (HttpClientErrorException) exception;
                        if (HttpStatus.NOT_FOUND.equals(errorException.getStatusCode())) {
                            return Response.builder().build();
                        }
                    }
                    throw new SimpleHttpException(exception.getMessage(), request);
                })
                .buildClient(PeopleRepository.class);

        Person p1 = new Person();
        p1.setName("Testy");
        p1.setAge(30);
        p1.setCity("San Francisco");
        p1.setAttributes(Map.of("fav_food", List.of("pizza", "soda", "potatoe chips")));
        Person created = peopleRepository.create(p1);
        assertThat(created).usingRecursiveComparison().ignoringFields("_id").isEqualTo(p1);

        Person fetched = peopleRepository.get(created.get_id());
        assertThat(fetched).usingRecursiveComparison().isEqualTo(created);

        List<Person> people = peopleRepository.list();
        assertThat(people).contains(created);

        Person toUpdate = new Person();
        toUpdate.setName("Testerson");
        toUpdate.setAge(25);
        toUpdate.setCity("San Jose");
        toUpdate.setAttributes(Map.of("fav_food", List.of("lettuce", "tomatoes")));
        peopleRepository.update(fetched.get_id(), toUpdate);
        Person updated = peopleRepository.get(fetched.get_id());
        toUpdate.set_id(fetched.get_id());
        assertThat(updated).usingRecursiveComparison().isEqualTo(toUpdate);

        peopleRepository.delete(updated.get_id());
        Person deleted = peopleRepository.get(fetched.get_id());
        assertThat(deleted).isNull();
    }
}