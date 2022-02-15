import com.simplehttp.core.client.AnnotationProcessor;
import com.simplehttp.core.client.ClientBuilder;
import com.simplehttp.core.client.model.ClientMetadata;
import org.springframework.web.bind.annotation.PostMapping;
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
//        TestClient client = new TestClientImpl();
//        String result = client.getMessage(2);
//        System.out.println(result);

//        TestClient client = getClient(TestClient.class);
//        String result = client.getMessage(2, null);
//        System.out.println(result);

//        AnnotationProcessor processor = new AnnotationProcessor();
//        ClientMetadata k = processor.extractClientMetadata(TestClient.class);
//        System.out.println(k);
        ClientBuilder clientBuilder = new ClientBuilder(null, new AnnotationProcessor());
        TestClient testClient = clientBuilder.buildClient(TestClient.class);
        testClient.testAllV2("token", Map.of("header_1", "header_1_value", "Authorization", "Bearer some_token"), "some_page",
                Map.of("query_1", "query_1_value"), "234353", List.of("this", "is", "payload"),
                "/partial");
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
