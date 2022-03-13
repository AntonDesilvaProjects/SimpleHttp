import com.simplehttp.core.annotation.client.SimpleHttpClient;
import com.simplehttp.core.annotation.http.PathParam;
import com.simplehttp.core.annotation.http.RequestAttribute;
import com.simplehttp.core.annotation.http.RequestBody;
import com.simplehttp.core.client.ClientBuilder;
import com.simplehttp.core.client.http.HttpMethod;
import com.simplehttp.httpclient.spring.Person;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

/**
 *  1. Annotated class - target
 *  2. Annotation Processor - takes the target + bootstraps a set of methods and
 *  3. Actual client class implementation
 * */
public class Runner {
    public static void main(String... args) {
        ClientBuilder clientBuilder = new ClientBuilder();
        PeopleRepository peopleRepository = clientBuilder
                .buildClient(PeopleRepository.class);

        Person p1 = new Person();
        p1.setName("Testy");
        p1.setAge(30);
        p1.setCity("San Francisco");
        p1.setAttributes(Map.of("fav_food", List.of("pizza", "soda", "potatoe chips")));
        Person created = peopleRepository.create(p1);
        System.out.println(created);

    }

    @SimpleHttpClient(host = "https://crudcrud.com/api/1295f14b76a74ec08e94eda42c7c80a0/person")
    interface PeopleRepository {
        @RequestAttribute(httpMethod = HttpMethod.POST, headers = {"Content-Type=application/json"})
        Person create(@RequestBody Person person);

        @RequestAttribute("/{id}")
        Person get(@PathParam("id") String id);

        @RequestAttribute
        List<Person> list();

        @RequestAttribute("/{id}")
        Person update(@PathParam("id") String id, @RequestBody Person person);

        @RequestAttribute("/{id}")
        void delete(String id);
    }


    public static <T> T getClient(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(HttpClientHandler.class.getClassLoader(),
                new Class[]{clazz}, new HttpClientHandler());
    }


    // this cl
    static class HttpClientHandler implements InvocationHandler {
        final RestTemplate restTemplate = new RestTemplate(); // client[could be any client]
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // this method will simply analyze the annotations for the invoked method + any arguments passed
            // to it to build a generic Request object and call client and send response.
            System.out.println(method.getName());

            // use method signature to get cached request template

            // get the arguments passed to method
            int accountId = (int) args[0];
            Object result = null;

            // "render" the request

            // client invocation
            try {
                // invoke pre-request handler
                result = restTemplate.getForEntity("https://testytesterson.free.beeceptor.com/test/" + accountId, String.class).getBody();
                // invoke post-request handlers
                // end client invocation
            } catch (Exception e) {
                // invoke error handlers
            }
            return result;
        }
    }
}
