package com.simplehttp.core.annotation;

import com.simplehttp.core.annotation.client.SimpleHttpClient;
import com.simplehttp.core.client.model.ClientMetadata;

/**
 * Annotation processor for {@link SimpleHttpClient} client.
 */
public interface AnnotationProcessor {
    /**
     * Generates a metadata configuration for a target client class annotated with
     * the {@link SimpleHttpClient} annotation.
     *
     * @param target annotated class to process
     * @return {@link ClientMetadata} object containing extracted metadata for the target
     */
    ClientMetadata extractClientMetadata(final Class<?> target);
}
