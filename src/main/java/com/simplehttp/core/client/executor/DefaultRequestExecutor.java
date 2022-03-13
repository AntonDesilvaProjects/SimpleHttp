package com.simplehttp.core.client.executor;

import com.simplehttp.core.client.HttpClient;
import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.model.Response;
import com.simplehttp.core.exception.SimpleHttpException;
import com.simplehttp.utils.Utils;

import java.util.List;

/**
 * A basic RequestExecutor that executes a Request and handle errors.
 */
public class DefaultRequestExecutor extends AbstractRequestExecutor {

    @Override
    public Response executeRequest(Request request, HttpClient httpClient, List<ErrorHandler> errorHandlers) {
        final Response response;
        try {
            response = httpClient.execute(request);
        } catch (Exception e) {
            if (Utils.isEmpty(errorHandlers)) {
                throw new SimpleHttpException(String.format("Error while executing request [%s] %s",
                        request.getHttpMethod(), request.getUrl()), request, e);
            }
            return handleErrors(request, null, e, errorHandlers);
        }
        return response;
    }
}
