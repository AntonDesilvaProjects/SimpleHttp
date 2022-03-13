import com.simplehttp.provider.spring.Person;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestClientImpl implements TestClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getMessage(int accountId) {
        return restTemplate.getForEntity("https://testytesterson.free.beeceptor.com/test/" + accountId, String.class).getBody();
    }

    @Override
    public String getMessage(int accountId, String s) {
        return null;
    }

    @Override
    public String testAll(String something, Map<String, String> extraHeaders, Map<String, List<String>> extraHeadersList, LinkedHashMap<String, List<String>> extraHeadersList2) {
        return null;
    }

    @Override
    public String testAllV2(String token, Map<String, String> dynamicHeaders, String page, Map<String, String> dynamicQueryParams, String accountId, List<String> payload, String s) {
        return null;
    }

    @Override
    public void getPeople(Person p) {
        //return null;
    }
}
