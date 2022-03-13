import com.simplehttp.core.annotation.client.SimpleHttpClient;
import com.simplehttp.core.annotation.http.*;
import com.simplehttp.core.client.http.HttpMethod;
import com.simplehttp.httpclient.spring.Person;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//https://testytesterson.free.beeceptor.com/test
@SimpleHttpClient(name = "testClient", host = "https://testytesterson.free.beeceptor.com")
public interface  TestClient {
    @RequestAttribute(value = "/test/{accountId}", headers = {})
    String getMessage(@PathParam("accountId") int accountId);

    String getMessage(@PathParam("accountId") int accountId, String s);

    @RequestAttribute(value = "/test/{accountId}", headers = {})
    String testAll(@HttpHeader("Authorization") String something,
                   @HttpHeader Map<String, String> extraHeaders,
                   @HttpHeader Map<String, List<String>> extraHeadersList,
                   @HttpHeader LinkedHashMap<String, List<String>> extraHeadersList2);

    @RequestAttribute(
            httpMethod = HttpMethod.PATCH,
            value = "/test/{accountId}",
            headers = {"Content-Type=application/json", "x-request-name=all-values"},
            queryParams = {"page_size=256", "order=ASC"}
    )
    String testAllV2(@HttpHeader("Authorization") String token,
                     @HttpHeader Map<String, String> dynamicHeaders,
                     @QueryParam("page") String page,
                     @QueryParam Map<String, String> dynamicQueryParams,
                     @PathParam("accountId") String accountId,
                     @RequestBody List<String> payload, @Url String s);


    @RequestAttribute(value = "https://webhook.site/7faad5d8-bb2e-400b-a79b-d7d61a2d16a4", httpMethod = HttpMethod.POST)
    void getPeople(@RequestBody Person person);


}
