package io.whalebone.publicapi.rest;

import com.google.gson.Gson;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Dependent
@Produces(MediaType.APPLICATION_JSON)
/*@Consumes(MediaType.APPLICATION_JSON)*/
public class GsonProvider<T> implements MessageBodyWriter<T> /*, MessageBodyReader<T>*/ {

    @Inject
    @Api
    private Gson gson;

//    @Override
//    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
//        return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
//    }
//
//    @Override
//    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
//        try (Reader reader = new InputStreamReader(entityStream)) {
//            return gson.fromJson(reader, type);
//        }
//    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
    }

    @Override
    public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try(Writer writer = new OutputStreamWriter(entityStream)) {
            gson.toJson(t, writer);
        }
    }
}
