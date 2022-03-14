# SimpleHTTP Client
A simple HTTP client inspired by Retrofit & Feign. Uses annotation based configuration to quickly setup an HTTP client 
for any REST endpoint. 

## Example configuration
A basic configuration example:

1. Create annotated class with HTTP Request information.
```java
 @SimpleHttpClient(host = "https://person-api.com")
 interface PersonRepository {
     @RequestAttribute(httpMethod = HttpMethod.POST, 
             headers = {"Content-Type=application/json"})
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
```
Supports all standard HTTP methods, custom headers, query params, etc.
2. Use the `SimpleHTTP` client builder to get a client instance:

```java
PersonRepository personRepository = new ClientBuilder().buildClient(PeopleRepository.class);
```
3. Use the instance to interact with the remote API:

```java
List<Person> persons = personRepository.list();
```
## Other configuration options
A request with some other possible request configuration options. 
```java
@RequestAttribute(
        httpMethod = HttpMethod.PATCH,
        value = "/test/{accountId}",
        headers = {"Content-Type=application/json", "x-request-name=all-values"},
        queryParams = {"page_size=256", "order=ASC"}
)
String all(@HttpHeader("Authorization") String token,
           @HttpHeader Map<String, String> dynamicHeaders,
           @QueryParam("page") String page,
           @QueryParam Map<String, String> dynamicQueryParams,
           @PathParam("accountId") String accountId,
           @RequestBody List<String> payload, 
           @Url String someUrl)
```


Below are some advanced configuration options for `SimpleHTTP` builder:
```java
new ClientBuilder()
        .withRequestInterceptors(...) // handlers to execute before a request
        .withResponseInterceptors(...) // handlers to execute on a receieved response
        .withErrorHandler(...) // error handlers
        .withHttpClient(...) // configure a custom HTTP client to use
        .withExecutionHandler(...) // configure a custom request executor for a more fine-grained request 
        // orchestration(load-balancing, retries, etc)
        .withAnnotationProcessor(...) // setup a custom annotation parser
        .buildClient(PeopleRepository.class);
```