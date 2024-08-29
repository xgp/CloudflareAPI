/*
 * Copyright (c) RoboFlax. All rights reserved.
 * Use is subject to license terms.
 */
package eu.roboflax.cloudflare;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.roboflax.cloudflare.constants.Category;
import eu.roboflax.cloudflare.http.HttpMethod;
import io.joshworks.restclient.http.HttpResponse;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used for creating cloudflare requests.
 */
public class CloudflareRequest {
    
    @Getter
    private CloudflareAccess cloudflareAccess;
    
    private HttpMethod httpMethod;
    private String additionalPath;
    private List<String> orderedIdentifiers = Lists.newArrayList();
    private Map<String, Object> queryStrings = Maps.newHashMap();
    private JsonObject body = new JsonObject();
    
    private Pair<HttpResponse<String>, JsonObject> response;
    
    public static final String ERROR_INVALID_ADDITIONAL_PATH = "you have to specify the additional path";
    public static final String ERROR_INVALID_HTTP_METHOD = "you have to specify the http method";
    public static final String ERROR_INVALID_BODY = "invalid body";
    public static final String ERROR_INVALID_QUERY_STRING = "invalid query string";
    public static final String ERROR_INVALID_IDENTIFIER = "invalid identifier";
    public static final String ERROR_INVALID_PAGINATION = "invalid pagination";
    
    public static final String ERROR_RESULT_IS_JSON_OBJECT = "Property 'result' is not a json array, because it is a json object use asObject() instead of asObjectList().";
    public static final String ERROR_RESULT_IS_JSON_ARRAY = "Property 'result' is not a json object, because it is a json array use asObjectList() instead of asObject().";
    
    public static final String ERROR_PARSING_JSON = "Could not parse returned text as json.";
    
    /**
     * The http request still was successful. If you used a CloudflareCallback for this request then onFailure will be executed.
     * Otherwise if you have a CloudflareResponse object you can still use .getJson() to retrieve further information.
     */
    public static final String ERROR_CLOUDFLARE_FAILURE = "Cloudflare was unable to perform your request and couldn't determine the result of the requested/passed information. " +
            "Maybe you built the request wrong or something different.";
    
    
    public CloudflareRequest( ) {
    }
    
    public CloudflareRequest( HttpMethod httpMethod ) {
        httpMethod( httpMethod );
    }
    
    public CloudflareRequest( String additionalPath ) {
        additionalPath( additionalPath );
    }
    
    public CloudflareRequest( CloudflareAccess cloudflareAccess ) {
        cloudflareAccess( cloudflareAccess );
    }
    
    public CloudflareRequest( HttpMethod httpMethod, CloudflareAccess cloudflareAccess ) {
        httpMethod( httpMethod ).cloudflareAccess( cloudflareAccess );
    }
    
    public CloudflareRequest( HttpMethod httpMethod, String additionalPath ) {
        httpMethod( httpMethod ).additionalPath( additionalPath );
    }
    
    public CloudflareRequest( String additionalPath, CloudflareAccess cloudflareAccess ) {
        additionalPath( additionalPath ).cloudflareAccess( cloudflareAccess );
    }
    
    public CloudflareRequest( HttpMethod httpMethod, String additionalPath, CloudflareAccess cloudflareAccess ) {
        httpMethod( httpMethod ).additionalPath( additionalPath ).cloudflareAccess( cloudflareAccess );
    }
    
    public CloudflareRequest( Category category, CloudflareAccess cloudflareAccess ) {
        category( category ).cloudflareAccess( cloudflareAccess );
    }
    
    public CloudflareRequest( Category category ) {
        category( category );
    }
    
    public static CloudflareRequest newRequest( ) {
        return new CloudflareRequest();
    }
    
    public static CloudflareRequest newRequest( HttpMethod httpMethod ) {
        return new CloudflareRequest( httpMethod );
    }
    
    public static CloudflareRequest newRequest( String additionalPath ) {
        return new CloudflareRequest( additionalPath );
    }
    
    public static CloudflareRequest newRequest( CloudflareAccess cloudflareAccess ) {
        return new CloudflareRequest( cloudflareAccess );
    }
    
    public static CloudflareRequest newRequest( HttpMethod httpMethod, String additionalPath ) {
        return new CloudflareRequest( httpMethod, additionalPath );
    }
    
    public static CloudflareRequest newRequest( String additionalPath, CloudflareAccess cloudflareAccess ) {
        return new CloudflareRequest( additionalPath, cloudflareAccess );
    }
    
    public static CloudflareRequest newRequest( HttpMethod httpMethod, CloudflareAccess cloudflareAccess ) {
        return new CloudflareRequest( httpMethod, cloudflareAccess );
    }
    
    public static CloudflareRequest newRequest( HttpMethod httpMethod, String additionalPath, CloudflareAccess cloudflareAccess ) {
        return new CloudflareRequest( httpMethod, additionalPath, cloudflareAccess );
    }
    
    public static CloudflareRequest newRequest( Category category ) {
        return new CloudflareRequest( category );
    }
    
    public static CloudflareRequest newRequest( Category category, CloudflareAccess cloudflareAccess ) {
        return new CloudflareRequest( category, cloudflareAccess );
    }
    
    /**
     * Sets the additional path which will be appended on {@link eu.roboflax.cloudflare.CloudflareAccess#API_BASE_URL}.
     * Not usable when {@link eu.roboflax.cloudflare.constants.Category} was given.
     *
     * @param additionalPath
     * @return
     */
    public CloudflareRequest additionalPath( String additionalPath ) {
        this.additionalPath = validAdditionalPath( checkNotNull( additionalPath ) );
        return this;
    }
    
    public CloudflareRequest httpMethod( HttpMethod httpMethod ) {
        this.httpMethod = checkNotNull( httpMethod );
        return this;
    }
    
    public CloudflareRequest category( Category category ) {
        checkNotNull( category );
        httpMethod( category.getHttpMethod() ).additionalPath( category.getAdditionalPath() );
        return this;
    }
    
    public CloudflareRequest cloudflareAccess( CloudflareAccess cloudflareAccess ) {
        this.cloudflareAccess = checkNotNull( cloudflareAccess );
        return this;
    }
    
    public CloudflareRequest identifiers( String... orderedIdentifiers ) {
        checkNotNull( orderedIdentifiers, ERROR_INVALID_IDENTIFIER );
        if ( Lists.newArrayList( orderedIdentifiers ).contains( null ) )
            throw new NullPointerException( ERROR_INVALID_IDENTIFIER );
        Collections.addAll( this.orderedIdentifiers, orderedIdentifiers );
        return this;
    }
    
    public CloudflareRequest queryString( String parameter, Object value ) {
        queryStrings.put( checkNotNull( parameter, ERROR_INVALID_QUERY_STRING ), checkNotNull( value, ERROR_INVALID_QUERY_STRING ) );
        return this;
    }
    
    public CloudflareRequest queryString( Map<String, Object> parameterValue ) {
        checkNotNull( parameterValue, ERROR_INVALID_QUERY_STRING );
        for ( Map.Entry<String, Object> e : parameterValue.entrySet() )
            queryString( e.getKey(), e.getValue() );
        return this;
    }
    
    public CloudflareRequest queryString( String parameter, Collection<?> values ) {
        checkNotNull( parameter, ERROR_INVALID_QUERY_STRING );
        checkNotNull( values, ERROR_INVALID_QUERY_STRING );
        for ( Object value : values )
            queryString( parameter, value );
        return this;
    }
    
    public CloudflareRequest body( JsonObject wholeBody ) {
        body = checkNotNull( wholeBody );
        return this;
    }
    
    public CloudflareRequest body( JsonElement wholeBody ) {
        body( checkNotNull( wholeBody ).getAsJsonObject() );
        return this;
    }
    
    public CloudflareRequest body( String wholeBody ) {
        body( new JsonParser().parse( checkNotNull( wholeBody ) ) );
        return this;
    }
    
    public CloudflareRequest body( String property, String value ) {
        body.addProperty( checkNotNull( property, ERROR_INVALID_BODY ), checkNotNull( value, ERROR_INVALID_BODY ) );
        return this;
    }
    
    public CloudflareRequest body( String property, Number value ) {
        body.addProperty( checkNotNull( property, ERROR_INVALID_BODY ), checkNotNull( value, ERROR_INVALID_BODY ) );
        return this;
    }
    
    public CloudflareRequest body( String property, Boolean value ) {
        body.addProperty( checkNotNull( property, ERROR_INVALID_BODY ), checkNotNull( value, ERROR_INVALID_BODY ) );
        return this;
    }
    
    public CloudflareRequest body( String property, Character value ) {
        body.addProperty( checkNotNull( property, ERROR_INVALID_BODY ), checkNotNull( value, ERROR_INVALID_BODY ) );
        return this;
    }
    
    public CloudflareRequest body( String property, JsonElement value ) {
        body.add( checkNotNull( property, ERROR_INVALID_BODY ), checkNotNull( value, ERROR_INVALID_BODY ) );
        return this;
    }
    
    public CloudflareRequest pagination( Pagination pagination ) {
        queryString( checkNotNull( pagination, ERROR_INVALID_PAGINATION ).getAsQueryStringsMap() );
        return this;
    }
    
    private HttpResponse<String> sendRequest( ) {
        switch ( checkNotNull( httpMethod, ERROR_INVALID_HTTP_METHOD ) ) {
            case GET:
                return cloudflareAccess.getRestClient()
                        .get( categoryPath() )
                        .queryString( queryStrings )
                        .asString();
            case POST:
                return cloudflareAccess.getRestClient()
                        .post( categoryPath() )
                        .queryString( queryStrings )
                        .body( body.toString() )
                        .asString();
            case DELETE:
                return cloudflareAccess.getRestClient()
                        .delete( categoryPath() )
                        .queryString( queryStrings )
                        .body( body.toString() )
                        .asString();
            case PUT:
                return cloudflareAccess.getRestClient()
                        .put( categoryPath() )
                        .queryString( queryStrings )
                        .body( body.toString() )
                        .asString();
            case PATCH:
                return cloudflareAccess.getRestClient()
                        .patch( categoryPath() )
                        .queryString( queryStrings )
                        .body( body.toString() )
                        .asString();
            default:
                throw new IllegalStateException( "Should never happen because other http methods are blocked." );
        }
    }
    
    private Pair<HttpResponse<String>, JsonObject> response( ) {
        if ( response == null ) {
            HttpResponse<String> httpResponse = sendRequest();
            JsonElement parsed = new JsonParser().parse( httpResponse.body() );
            // Check if parsing was successful, gson returns json null if failed
            if ( parsed.isJsonNull() )
                throw new IllegalStateException( ERROR_PARSING_JSON );
            response = Pair.of( httpResponse, parsed.getAsJsonObject() );
        }
        return response;
    }
    
    /*
     * asVoid
     * asVoid
     * asVoid
     * asVoid
     * asVoid
     * asVoid
     * asVoid
     * asVoid
     * asVoid
     * asVoid
     */
    
    /**
     * Sends request. No object mapping and/or object parsing will be handled.
     *
     * @return CloudflareResponse<Void>
     */
    public CloudflareResponse<Void> asVoid( ) {
        HttpResponse<String> httpResponse = response().getLeft();
        JsonObject json = response().getRight();
        return new CloudflareResponse<>(
                json,
                null,
                httpResponse.isSuccessful(),
                httpResponse.getStatus(),
                httpResponse.getStatusText()
        );
    }
    
    /**
     * Consumer method for: {@link CloudflareRequest#asVoid()}
     */
    public void asVoid( Consumer<CloudflareResponse<Void>> consumer ) {
        consumer.accept( asVoid() );
    }
    
    /**
     * Async callback method for {@link CloudflareRequest#asVoid()}
     */
    public void asVoidAsync( CloudflareCallback<CloudflareResponse<Void>> callback ) {
        asyncCallback( callback, this::asVoid );
    }
    
    /**
     * Sync callback method for {@link CloudflareRequest#asVoid()}
     */
    public void asVoid( CloudflareCallback<CloudflareResponse<Void>> callback ) {
        syncCallback( callback, this::asVoid );
    }
    
    /**
     * Async method for {@link CloudflareRequest#asVoid()}
     */
    public CompletableFuture<CloudflareResponse<Void>> asVoidAsync( ) {
        return CompletableFuture.supplyAsync( this::asVoid, getCloudflareAccess().getThreadPool() );
    }
    
    /**
     * Same as {@link CloudflareRequest#asVoid()}
     */
    public CloudflareResponse<Void> send( ) {
        return asVoid();
    }
    
    /**
     * Same as {@link CloudflareRequest#asVoid()} but async
     */
    public CompletableFuture<CloudflareResponse<Void>> sendAsync( ) {
        return asVoidAsync();
    }
    
    /**
     * Same as {@link CloudflareRequest#asVoid()} but with consumer
     */
    public void send( Consumer<CloudflareResponse<Void>> consumer ) {
        consumer.accept( asVoid() );
    }
    
    /**
     * Same as {@link CloudflareRequest#asVoid()} but with async callback
     */
    public void sendAsync( CloudflareCallback<CloudflareResponse<Void>> callback ) {
        asyncCallback( callback, this::asVoid );
    }
    
    /**
     * Same as {@link CloudflareRequest#asVoid()} but with sync callback
     */
    public void send( CloudflareCallback<CloudflareResponse<Void>> callback ) {
        syncCallback( callback, this::asVoid );
    }
    
    /*
     * asObject
     * asObject
     * asObject
     * asObject
     * asObject
     * asObject
     * asObject
     * asObject
     * asObject
     * asObject
     */
    
    /**
     * Sends request. Parses the json result as the object type.
     *
     * @param objectType class of object
     * @param <T>        type of object
     * @return CloudflareResponse<T>
     */
    public <T> CloudflareResponse<T> asObject( Class<T> objectType ) {
        HttpResponse<String> httpResponse = response().getLeft();
        JsonObject json = response().getRight();
        if ( json.get( "result" ).isJsonObject() ) {
            return new CloudflareResponse<>(
                    json,
                    CloudflareAccess.getGson().fromJson( json.getAsJsonObject( "result" ), objectType ),
                    httpResponse.isSuccessful(),
                    httpResponse.getStatus(),
                    httpResponse.getStatusText()
            );
        }
        if ( json.get( "result" ).isJsonArray() )
            throw new IllegalStateException( ERROR_RESULT_IS_JSON_ARRAY );
        
        return new CloudflareResponse<>(
                json,
                null,
                httpResponse.isSuccessful(),
                httpResponse.getStatus(),
                httpResponse.getStatusText()
        );
    }
    
    /**
     * Async method for {@link CloudflareRequest#asObject(Class)}
     */
    public <T> CompletableFuture<CloudflareResponse<T>> asObjectAsync( Class<T> objectType ) {
        return CompletableFuture.supplyAsync( ( ) -> asObject( objectType ), getCloudflareAccess().getThreadPool() );
    }
    
    /**
     * Consumer method for {@link CloudflareRequest#asObject(Class)}
     */
    public <T> void asObject( Consumer<CloudflareResponse<T>> consumer, Class<T> objectType ) {
        consumer.accept( asObject( objectType ) );
    }
    
    /**
     * Async callback method for {@link CloudflareRequest#asObject(Class)}
     */
    public <T> void asObjectAsync( CloudflareCallback<CloudflareResponse<T>> callback, Class<T> objectType ) {
        asyncCallback( callback, ( ) -> asObject( objectType ) );
    }
    
    /**
     * Sync callback method for {@link CloudflareRequest#asObject(Class)}
     */
    public <T> void asObject( CloudflareCallback<CloudflareResponse<T>> callback, Class<T> objectType ) {
        syncCallback( callback, ( ) -> asObject( objectType ) );
    }
    
    /*
     * asObjectList
     * asObjectList
     * asObjectList
     * asObjectList
     * asObjectList
     * asObjectList
     * asObjectList
     */
    
    /**
     * Sends request. Parses and maps all entries in the json array result as a List<object type>.
     *
     * @param objectType class of object
     * @param <T>        type of object
     * @return CloudflareResponse
     */
    public <T> CloudflareResponse<List<T>> asObjectList( Class<T> objectType ) {
        JsonObject json = response().getRight();
        HttpResponse<String> httpResponse = response().getLeft();
        
        if ( json.get( "result" ).isJsonArray() ) {
            return new CloudflareResponse<>(
                    json,
                    toListOfObjects( json.getAsJsonArray( "result" ), objectType ),
                    httpResponse.isSuccessful(),
                    httpResponse.getStatus(),
                    httpResponse.getStatusText()
            );
        }
        if ( json.get( "result" ).isJsonObject() )
            throw new IllegalStateException( ERROR_RESULT_IS_JSON_OBJECT );
        
        return new CloudflareResponse<>(
                json,
                null,
                httpResponse.isSuccessful(),
                httpResponse.getStatus(),
                httpResponse.getStatusText()
        );
    }
    
    /**
     * Async method for {@link CloudflareRequest#asObjectList(Class)}
     */
    public <T> CompletableFuture<CloudflareResponse<List<T>>> asObjectListAsync( Class<T> objectType ) {
        return CompletableFuture.supplyAsync( ( ) -> asObjectList( objectType ), getCloudflareAccess().getThreadPool() );
    }
    
    /**
     * Consumer method for {@link CloudflareRequest#asObjectList(Class)}
     */
    public <T> void asObjectList( Consumer<CloudflareResponse<List<T>>> consumer, Class<T> objectType ) {
        consumer.accept( asObjectList( objectType ) );
    }
    
    /**
     * Async callback method for {@link CloudflareRequest#asObjectList(Class)}
     */
    public <T> void asObjectListAsync( CloudflareCallback<CloudflareResponse<List<T>>> callback, Class<T> objectType ) {
        asyncCallback( callback, ( ) -> asObjectList( objectType ) );
    }
    
    /**
     * Sync callback method for {@link CloudflareRequest#asObjectList(Class)}
     */
    public <T> void asObjectList( CloudflareCallback<CloudflareResponse<List<T>>> callback, Class<T> objectType ) {
        syncCallback( callback, ( ) -> asObjectList( objectType ) );
    }
    
    /*
     * asObjectOrObjectList
     * asObjectOrObjectList
     * asObjectOrObjectList
     * asObjectOrObjectList
     * asObjectOrObjectList
     * asObjectOrObjectList
     * asObjectOrObjectList
     */
    
    /**
     * Sends the request.
     * The "object" attribute in the returned CloudflareResponse<T> object is variable.
     * It checks if the returned "result" property in the cloudflare response is a json array, json object or json null
     * and adjusts it to the right object with the right type T.
     * <p>
     * Cast "object" of type T in the CloudflareResponse as you need, T or List<T>.
     * <p>
     * You can use it as follows:
     * * <pre>
     * {@code
     * CloudflareResponse response = new CloudflareRequest( Category.LIST_DNS_RECORDS, cloudflareAccess )
     *    .identifiers( zoneId )
     *    .asObjectOrObjectList( DNSRecord.class );
     * List<DNSRecord> records = (List<DNSRecord>) response.getObject();
     * <p>
     * {@code
     * CloudflareResponse response = new CloudflareRequest( Category.DNS_RECORD_DETAILS, cloudflareAccess )
     *    .identifiers( zoneId, "dns_record_id" )
     *    .asObjectOrObjectList( DNSRecord.class );
     * DNSRecord record = (DNSRecord) response.getObject();
     * }
     * </pre>
     *
     * @param objectType class of object type
     * @param <T>        will be the type of the "object" attribute in the returned CloudflareResponse<T> (or List<T>)
     * @return the CloudflareResponse<T> with attribute "object" as type T or List<T>
     */
    public <T> CloudflareResponse<T> asObjectOrObjectList( Class<T> objectType ) {
        JsonObject json = response().getRight();
        HttpResponse<String> httpResponse = response().getLeft();
        
        T object;
        // Check if result is json array.
        if ( json.get( "result" ).isJsonArray() ) {
            // Map object from json array to object list.
            object = (T) toListOfObjects( json.getAsJsonArray( "result" ), objectType );
        } else if ( json.get( "result" ).isJsonObject() )
            // json is a json object and the object is not mapped in a List
            object = CloudflareAccess.getGson().fromJson( json.getAsJsonObject( "result" ), objectType );
        else object = null;
        
        // Return the response
        return new CloudflareResponse<>(
                json,
                object,
                httpResponse.isSuccessful(),
                httpResponse.getStatus(),
                httpResponse.getStatusText()
        );
    }
    
    /**
     * Async method for {@link CloudflareRequest#asObjectOrObjectList(Class)}
     */
    public <T> CompletableFuture<CloudflareResponse<T>> asObjectOrObjectListAsync( Class<T> objectType ) {
        return CompletableFuture.supplyAsync( ( ) -> asObjectOrObjectList( objectType ), getCloudflareAccess().getThreadPool() );
    }
    
    /**
     * Consumer method for {@link CloudflareRequest#asObjectOrObjectList(Class)}
     */
    public <T> void asObjectOrObjectList( Consumer<CloudflareResponse<T>> consumer, Class<T> objectType ) {
        consumer.accept( asObjectOrObjectList( objectType ) );
    }
    
    /**
     * Async callback method for {@link CloudflareRequest#asObjectOrObjectList(Class)}
     */
    public <T> void asObjectOrObjectListAsync( CloudflareCallback<CloudflareResponse<T>> callback, Class<T> objectType ) {
        asyncCallback( callback, ( ) -> asObjectOrObjectList( objectType ) );
    }
    
    /**
     * Sync callback method for {@link CloudflareRequest#asObjectOrObjectList(Class)}
     */
    public <T> void asObjectOrObjectList( CloudflareCallback<CloudflareResponse<T>> callback, Class<T> objectType ) {
        syncCallback( callback, ( ) -> asObjectOrObjectList( objectType ) );
    }
    
    /*
     * INTERNAL HELPER METHODS
     * INTERNAL HELPER METHODS
     * INTERNAL HELPER METHODS
     * INTERNAL HELPER METHODS
     * INTERNAL HELPER METHODS
     * INTERNAL HELPER METHODS
     * INTERNAL HELPER METHODS
     * INTERNAL HELPER METHODS
     */
    
    /**
     * INTERNAL HELPER METHOD!
     *
     * @param callback    user'S callback
     * @param getResponse .call() is returning the CloudflareResponse<T>
     * @param <T>         type of "object" in CloudflareResponse
     */
    private <T> void runCallback( CloudflareCallback<CloudflareResponse<T>> callback, Callable<CloudflareResponse<T>> getResponse ) {
        HttpResponse<String> httpResponse = response().getLeft();
        JsonObject json = response().getRight();
        
        Throwable throwable;
        try {
            CloudflareResponse<T> response = getResponse.call();
            // http request successful
            
            // check "success" state in json -> cloudflare couldn't find the result
            if ( !response.isSuccessful() ) {
                throw new IllegalStateException( ERROR_CLOUDFLARE_FAILURE );
            }
            
            try { // Don't run onFailure when onSuccess throws an exception.
                callback.onSuccess( response );
            } catch ( Exception | Error e ) {
                e.printStackTrace();
            }
            return;
        } catch ( ExecutionException e ) {
            throwable = e.getCause();
        } catch ( Exception | Error e ) {
            throwable = e;
        }
        // Errors passed by Cloudflare
        Map<Integer, String> errors = Maps.newHashMap();
        
        JsonObject o;
        for ( JsonElement e : json.getAsJsonArray( "errors" ) ) {
            o = e.getAsJsonObject();
            errors.put( o.get( "code" ).getAsInt(), o.get( "message" ).getAsString() );
        }
        
        callback.onFailure( throwable, httpResponse.getStatus(), httpResponse.getStatusText(), errors );
    }
    
    /**
     * INTERNAL HELPER METHOD!
     * <p>
     * Wrapping {@link eu.roboflax.cloudflare.CloudflareRequest#runCallback(CloudflareCallback, Callable)} as synced callback.
     */
    private <T> void syncCallback( CloudflareCallback<CloudflareResponse<T>> callback, Callable<CloudflareResponse<T>> getResponse ) {
        runCallback( callback, getResponse );
    }
    
    /**
     * INTERNAL HELPER METHOD!
     * <p>
     * Wrapping {@link eu.roboflax.cloudflare.CloudflareRequest#runCallback(CloudflareCallback, Callable)} as async callback.
     */
    private <T> void asyncCallback( CloudflareCallback<CloudflareResponse<T>> callback, Callable<CloudflareResponse<T>> getResponse ) {
        ListenableFuture<CloudflareResponse<T>> future = MoreExecutors
                .listeningDecorator( getCloudflareAccess().getThreadPool() )
                .submit( getResponse );
        future.addListener( ( ) -> runCallback( callback, future::get ), getCloudflareAccess().getThreadPool() );
    }
    
    /**
     * INTERNAL HELPER METHOD!
     * <p>
     * Parse json array to list of objects.
     */
    private <T> List<T> toListOfObjects( JsonArray jsonArray, Class<T> objectType ) {
        return CloudflareAccess.getGson()
                .fromJson( jsonArray,
                        new ParameterizedType() {
                            @Override
                            public Type[] getActualTypeArguments( ) {
                                return new Type[]{objectType};
                            }
                            
                            @Override
                            public Type getRawType( ) {
                                return List.class;
                            }
                            
                            @Override
                            public Type getOwnerType( ) {
                                return null;
                            }
                        } );
    }
    
    /**
     * INTERNAL HELPER METHOD!
     *
     * @return formatted url containing the passed ordered identifiers replaced with {id-ORDER_NUMBER}
     */
    private String categoryPath( ) {
        String additionalCategoryPath = checkNotNull( additionalPath, ERROR_INVALID_ADDITIONAL_PATH );
        
        // pattern is like 'foo/{id-1}/bar/{id-2}'
        for ( int place = 1; place <= orderedIdentifiers.size(); place++ )
            additionalCategoryPath = additionalCategoryPath.replace( "{id-" + place + "}", orderedIdentifiers.get( place - 1 ) );
        
        return additionalCategoryPath;
    }
    
    /**
     * INTERNAL HELPER METHOD!
     * <p>
     * Some format checks of additional path.
     *
     * @param additionalPath
     * @return validated additional path
     */
    private static String validAdditionalPath( String additionalPath ) {
        if ( additionalPath.startsWith( "/" ) )
            additionalPath = additionalPath.substring( 1 );
        return additionalPath;
    }
}
