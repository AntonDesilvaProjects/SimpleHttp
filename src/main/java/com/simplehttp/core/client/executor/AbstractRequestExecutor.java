package com.simplehttp.core.client.executor;

import com.simplehttp.core.client.HttpClient;
import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.model.Response;
import com.simplehttp.core.exception.ErrorHandlingException;
import com.simplehttp.core.exception.ResponseInterceptorException;
import com.simplehttp.core.exception.RequestInterceptorException;
import com.simplehttp.core.exception.SimpleHttpException;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

/**
 * Abstract RequestExecutor implementation that provides the core functionalities for orchestrating
 * an HTTP request execution including chaining interceptors and error handlers.
 */
public abstract class AbstractRequestExecutor implements RequestExecutor {

    /**
     * Executes a request. Subclasses may implement to provide request execution logic such as error handling,
     * retries, client-side load-balancing etc.
     *
     * @param request request to execute
     * @param httpClient client used to execute request
     * @param errorHandlers list of error handlers
     * @return response
     */
    public abstract Response executeRequest(Request request, HttpClient httpClient, List<ErrorHandler> errorHandlers);

    /**
     * @inheritDoc
     */
    @Override
    public Object execute(Request request,
                          HttpClient httpClient,
                          List<RequestInterceptor> requestInterceptors,
                          List<ResponseInterceptor> responseInterceptors,
                          List<ErrorHandler> errorHandlers) throws SimpleHttpException {

        final Request requestToExecute = generateRequest(request, httpClient, requestInterceptors);
        Response response = executeRequest(request, httpClient, errorHandlers);
        final Response processedResponse = processResponse(requestToExecute, response, httpClient, responseInterceptors);
        return processedResponse.getParsedResponse();
    }

    protected Request generateRequest(Request originalRequest,
                                      HttpClient httpClient,
                                      List<RequestInterceptor> requestInterceptors) {

        final BiFunction<Request, RequestInterceptor, Request> requestProcessor = (currentRequest, executor) -> {
            try {
                return executor.process(currentRequest, httpClient);
            } catch (Exception e) {
                throw new RequestInterceptorException(String.format(
                        "Unexpected error while executing request processor: %s. Error: %s",
                        executor.getClass(), e.getLocalizedMessage()), originalRequest, executor, e);
            }
        };
        final Request processedRequest = chainHandlers(originalRequest, requestProcessor, requestInterceptors);
        return Optional.ofNullable(processedRequest)
                .orElseThrow(() -> new RequestInterceptorException("No request was provided", originalRequest));
    }

    protected Response processResponse(final Request executedRequest,
                                      Response originalResponse,
                                      final HttpClient httpClient,
                                      final List<ResponseInterceptor> responseInterceptors) {

        final BiFunction<Response, ResponseInterceptor, Response> responseProcessor = (currentResponse, executor) -> {
            try {
                return executor.process(currentResponse, executedRequest, httpClient);
            } catch (Exception e) {
                throw new ResponseInterceptorException(String.format(
                        "Unexpected error while executing response processor %s. Error: %s",
                        executor.getClass(), e.getLocalizedMessage()), executedRequest, currentResponse, executor, e);
            }
        };

        final Response processedResponse = chainHandlers(originalResponse, responseProcessor, responseInterceptors);

        return Optional.ofNullable(processedResponse)
                .orElseThrow(() -> new ResponseInterceptorException("No response was returned", executedRequest,
                        originalResponse));
    }

    protected Response handleErrors(final Request request, @Nullable Response response,
                                    Exception error, List<ErrorHandler> errorHandlers) {
        final BiFunction<Response, ErrorHandler, Response> responseProcessor = (currentResponse, executor) -> {
            try {
                return executor.handleError(request, currentResponse, error);
            } catch (Exception e) {
                throw new ErrorHandlingException(String.format("Unexpected error while invoking error handler %s. Error: %s",
                        executor.getClass(), e.getLocalizedMessage()), request, error, response, executor, e);
            }
        };
        final Response processedResponse = chainHandlers(response, responseProcessor, errorHandlers);

        return Optional.ofNullable(processedResponse)
                .orElseThrow(() -> new ResponseInterceptorException("No response was returned", request,
                        response));
    }

    private <T, R> T chainHandlers(T start, BiFunction<T, R, T> processor, List<R> executors) {
        // request/response/error handler directly mutate the passed in object, so we just take the latest value
        // without actual logic to combine the previous and the new value
        final BinaryOperator<T> lazyCombiner = (original, updated) -> updated;

        return Optional.ofNullable(executors)
                .orElse(List.of())
                .stream()
                .reduce(start, processor, lazyCombiner);
    }
}
