/*
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
 */
package com.spldeolin.allison1875.docanalyzer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * HTTP工具类
 * <pre>
 * 基于 OkHttp4 实现的HTTP请求工具类，提供GET和POST方法。
 * 支持泛化的返回类型或是JsonNode用于映射Response Body。
 * 默认总超时时间为10秒，可通过方法参数覆盖单次请求的超时时间。
 * </pre>
 *
 * @author Deolin 2025-11-15
 */
@Slf4j
public class HttpUtils {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json;charset=utf-8");

    private static final long DEFAULT_TIMEOUT_SEC = 10;

    private static final OkHttpClient DEFAULT_CLIENT = new OkHttpClient.Builder().callTimeout(DEFAULT_TIMEOUT_SEC,
            TimeUnit.SECONDS).connectionPool(new ConnectionPool(20, 300, TimeUnit.SECONDS)).build();

    private HttpUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 重载方法{@link #executePost(String, Object, TypeReference,
     * Map, OkHttpClient)}
     */
    public static JsonNode post(String url, Object reqBodyDTO) {
        return post(url, reqBodyDTO, null, null, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 重载方法{@link #executePost(String, Object, TypeReference,
     * Map, OkHttpClient)}
     */
    public static <T> T post(String url, Object reqBodyDTO, TypeReference<T> respBodyType) {
        return post(url, reqBodyDTO, respBodyType, null, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 重载方法{@link #executePost(String, Object, TypeReference,
     * Map, OkHttpClient)}
     */
    public static <T> T post(String url, Object reqBodyDTO, TypeReference<T> respBodyType,
            Map<String, String> reqHeaders) {
        return post(url, reqBodyDTO, respBodyType, reqHeaders, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 执行POST请求
     *
     * @param url 请求URL，不能为空
     * @param reqBodyDTO 请求体对象，将被序列化为JSON字符串，可以为null
     * @param respBodyType 响应体的类型引用，用于反序列化泛型JSON响应；可以为null，这将会返回JsonNode对象
     * @param reqHeaders 自定义HTTP请求头，key为header名称，value为header值，可以为null
     * @param timeoutSec 本次请求的总超时时间（秒），必须大于0
     * @param <T> 响应体的类型
     * @return 反序列化后的响应对象
     * @throws TimeoutException 请求超时时抛出
     * @throws Not200Exception 回应状态码非200时抛出
     * @throws HttpException 其他原因请求失败时抛出
     */
    public static <T> T post(String url, Object reqBodyDTO, TypeReference<T> respBodyType,
            Map<String, String> reqHeaders, long timeoutSec) throws Not200Exception, TimeoutException, HttpException {
        return executePost(url, reqBodyDTO, respBodyType, reqHeaders, createClient(timeoutSec));
    }

    private static <T> T executePost(String url, Object reqBodyDTO, TypeReference<T> respBodyType,
            Map<String, String> reqHeaders, OkHttpClient client) {
        String requestBodyJson = reqBodyDTO == null ? "" : JsonUtils.toJson(reqBodyDTO);
        RequestBody body = RequestBody.create(requestBodyJson, JSON_MEDIA_TYPE);

        Request request = buildRequest(url, body, reqHeaders);
        Call call = client.newCall(request);

        try {
            log.debug("go to execute post, curl={}", buildCurlCommand(request, requestBodyJson));
            Response response = call.execute();
            return handleResponse(response, request, respBodyType);
        } catch (InterruptedIOException e) {
            log.error("fail to execute post clause timeout, curl={}", buildCurlCommand(request, requestBodyJson), e);
            throw new TimeoutException();
        } catch (Exception e) {
            log.error("fail to execute post, curl={}", buildCurlCommand(request, requestBodyJson), e);
            throw new HttpException("远程请求失败");
        }
    }

    /**
     * 重载方法{@link #formPost(String, Map, TypeReference,
     * Map, long)}
     */
    public static JsonNode formPost(String url, Map<String, String> formData) {
        return formPost(url, formData, null, null, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 重载方法{@link #formPost(String, Map, TypeReference,
     * Map, long)}
     */
    public static <T> T formPost(String url, Map<String, String> formData, TypeReference<T> respBodyType) {
        return formPost(url, formData, respBodyType, null, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 重载方法{@link #formPost(String, Map, TypeReference,
     * Map, long)}
     */
    public static <T> T formPost(String url, Map<String, String> formData, TypeReference<T> respBodyType,
            Map<String, String> reqHeaders) {
        return formPost(url, formData, respBodyType, reqHeaders, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 执行POST请求，请求体为form data格式
     *
     * @param url 请求URL，不能为空
     * @param formData form data参数，key为参数名，value为参数值，可以为null或空Map
     * @param respBodyType 响应体的类型引用，用于反序列化泛型JSON响应；可以为null，这将会返回JsonNode对象
     * @param reqHeaders 自定义HTTP请求头，key为header名称，value为header值，可以为null
     * @param timeoutSec 本次请求的总超时时间（秒），必须大于0
     * @param <T> 响应体的类型
     * @return 反序列化后的响应对象
     * @throws TimeoutException 请求超时时抛出
     * @throws Not200Exception 回应状态码非200时抛出
     * @throws HttpException 其他原因请求失败时抛出
     */
    public static <T> T formPost(String url, Map<String, String> formData, TypeReference<T> respBodyType,
            Map<String, String> reqHeaders, long timeoutSec) throws Not200Exception, TimeoutException, HttpException {
        return executeFormPost(url, formData, respBodyType, reqHeaders, createClient(timeoutSec));
    }

    private static <T> T executeFormPost(String url, Map<String, String> formData, TypeReference<T> respBodyType,
            Map<String, String> reqHeaders, OkHttpClient client) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (formData != null && !formData.isEmpty()) {
            formData.forEach(formBuilder::add);
        }
        RequestBody body = formBuilder.build();

        Request request = buildRequest(url, body, reqHeaders);
        Call call = client.newCall(request);

        try {
            log.debug("go to execute form post, curl={}", buildCurlCommand(request, null));
            Response response = call.execute();
            return handleResponse(response, request, respBodyType);
        } catch (InterruptedIOException e) {
            log.error("fail to execute form post clause timeout, curl={}", buildCurlCommand(request, null), e);
            throw new TimeoutException();
        } catch (Exception e) {
            log.error("fail to execute form post, curl={}", buildCurlCommand(request, null), e);
            throw new HttpException("远程请求失败");
        }
    }

    /**
     * 构建HTTP请求对象
     *
     * @param url 请求URL
     * @param body 请求体
     * @param reqHeaders 请求头
     * @return Request对象
     */
    private static Request buildRequest(String url, RequestBody body, Map<String, String> reqHeaders) {
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);

        if (reqHeaders != null && !reqHeaders.isEmpty()) {
            reqHeaders.forEach(requestBuilder::addHeader);
        }

        return requestBuilder.build();
    }

    /**
     * 重载方法{@link #ssePost(String, Object, Consumer, Map, long)}
     */
    public static void ssePost(String url, Object reqBodyDTO, Consumer<String> eventConsumer) {
        ssePost(url, reqBodyDTO, eventConsumer, null, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 重载方法{@link #ssePost(String, Object, Consumer, Map, long)}
     */
    public static void ssePost(String url, Object reqBodyDTO, Consumer<String> eventConsumer,
            Map<String, String> reqHeaders) {
        ssePost(url, reqBodyDTO, eventConsumer, reqHeaders, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 执行POST请求，响应为SSE（Server-Sent Events）流
     * <p>
     * SSE响应是流式的，通过Consumer回调每个事件的数据。
     * SSE格式：每行以"data:"开头，后面是实际数据，事件之间用空行分隔。
     * </p>
     *
     * @param url 请求URL，不能为空
     * @param reqBodyDTO 请求体对象，将被序列化为JSON字符串，可以为null
     * @param eventConsumer 用于消费每个SSE事件数据的Consumer，每个事件的数据（去除"data:"前缀后的内容）会通过此Consumer回调
     * @param reqHeaders 自定义HTTP请求头，key为header名称，value为header值，可以为null
     * @param timeoutSec 本次请求的总超时时间（秒），必须大于0
     * @throws TimeoutException 请求超时时抛出
     * @throws Not200Exception 回应状态码非200时抛出
     * @throws HttpException 其他原因请求失败时抛出
     */
    public static void ssePost(String url, Object reqBodyDTO, Consumer<String> eventConsumer,
            Map<String, String> reqHeaders, long timeoutSec) throws Not200Exception, TimeoutException, HttpException {
        executeSsePost(url, reqBodyDTO, eventConsumer, reqHeaders, createClient(timeoutSec));
    }

    private static void executeSsePost(String url, Object reqBodyDTO, Consumer<String> eventConsumer,
            Map<String, String> reqHeaders, OkHttpClient client) {
        String requestBodyJson = reqBodyDTO == null ? "" : JsonUtils.toJson(reqBodyDTO);
        RequestBody body = RequestBody.create(requestBodyJson, JSON_MEDIA_TYPE);

        Request request = buildRequest(url, body, reqHeaders);
        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            log.debug("go to execute sse post, curl={}", buildCurlCommand(request, requestBodyJson));

            if (!response.isSuccessful()) {
                String errorBody = "";
                try (ResponseBody respBody = response.body()) {
                    errorBody = respBody != null ? respBody.string() : "";
                } catch (Exception e) {
                    log.warn("fail to read error body, curl={}", buildCurlCommand(request, requestBodyJson), e);
                }
                log.error("fail to execute sse clause not 200 code, curl={}, code={}, respBody={}",
                        buildCurlCommand(request, requestBodyJson), response.code(), errorBody);
                throw new Not200Exception(response.code());
            }

            ResponseBody respBody = response.body();
            if (respBody == null) {
                log.error("fail to execute sse clause null resp body, curl={}",
                        buildCurlCommand(request, requestBodyJson));
                throw new HttpException("远程请求失败");
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(respBody.byteStream(), StandardCharsets.UTF_8))) {
                StringBuilder eventData = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        // 提取data:后面的内容，支持多行data（用换行符连接）
                        String data = line.substring(5);
                        if (eventData.length() > 0) {
                            eventData.append("\n");
                        }
                        eventData.append(data);
                    } else if (line.trim().isEmpty()) {
                        // 空行表示一个事件结束
                        if (eventData.length() > 0) {
                            String eventDataStr = eventData.toString().trim();
                            log.debug("receive sse event, url={}, eventData={}", url, eventDataStr);
                            eventConsumer.accept(eventDataStr);
                            eventData.setLength(0); // 清空，准备下一个事件
                        }
                    } else if (line.startsWith("event:")) {
                        // 可选：处理event类型，这里暂时忽略
                        log.debug("receive sse event type, url={}, eventType={}", url, line.substring(6).trim());
                    } else if (line.startsWith("id:")) {
                        // 可选：处理event id，这里暂时忽略
                        log.debug("receive sse event id, url={}, eventId={}", url, line.substring(3).trim());
                    }
                }

                // 处理最后一个事件（如果没有以空行结尾）
                if (eventData.length() > 0) {
                    String eventDataStr = eventData.toString().trim();
                    log.debug("receive sse event (last), url={}, eventData={}", url, eventDataStr);
                    eventConsumer.accept(eventDataStr);
                }

                log.debug("sse stream completed, curl={}", buildCurlCommand(request, requestBodyJson));
            }
        } catch (InterruptedIOException e) {
            log.error("fail to execute sse clause timeout, curl={}", buildCurlCommand(request, requestBodyJson), e);
            throw new TimeoutException();
        } catch (Exception e) {
            log.error("fail to execute sse, curl={}", buildCurlCommand(request, requestBodyJson), e);
            throw new HttpException("远程请求失败");
        }
    }

    /**
     * 重载方法{@link #get(String, Map, TypeReference, Map,
     * long)}
     */
    public static JsonNode get(String url) {
        return get(url, null, null, null, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 重载方法{@link #get(String, Map, TypeReference, Map,
     * long)}
     */
    public static <T> T get(String url, TypeReference<T> respBodyType) {
        return get(url, respBodyType, null, null, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 重载方法{@link #get(String, Map, TypeReference, Map,
     * long)}
     */
    public static <T> T get(String url, TypeReference<T> respBodyType, Map<String, String> reqParams) {
        return get(url, respBodyType, reqParams, null, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 重载方法{@link #get(String, Map, TypeReference, Map,
     * long)}
     */
    public static <T> T get(String url, TypeReference<T> respBodyType, Map<String, String> reqParams,
            Map<String, String> reqHeaders) {
        return get(url, respBodyType, reqParams, reqHeaders, DEFAULT_TIMEOUT_SEC);
    }

    /**
     * 执行GET请求
     *
     * @param url 请求URL，不能为空
     * @param reqParams 请求参数，key为参数名，value为参数值，可以为null
     * @param respBodyType 响应体的类型引用，用于反序列化泛型JSON响应；可以为null，这将会返回JsonNode对象
     * @param reqHeaders 自定义HTTP请求头，key为header名称，value为header值，可以为null
     * @param timeoutSec 本次请求的总超时时间（秒），必须大于0
     * @param <T> 响应体的类型
     * @return 反序列化后的响应对象
     * @throws TimeoutException 请求超时时抛出
     * @throws Not200Exception 回应状态码非200时抛出
     * @throws HttpException 其他原因请求失败时抛出
     */
    public static <T> T get(String url, TypeReference<T> respBodyType, Map<String, String> reqParams,
            Map<String, String> reqHeaders, long timeoutSec) throws Not200Exception, TimeoutException, HttpException {
        return executeGet(url, respBodyType, reqParams, reqHeaders, createClient(timeoutSec));
    }

    private static <T> T executeGet(String url, TypeReference<T> respBodyType, Map<String, String> reqParams,
            Map<String, String> reqHeaders, OkHttpClient client) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (reqParams != null && !reqParams.isEmpty()) {
            reqParams.forEach(urlBuilder::addQueryParameter);
        }
        HttpUrl httpUrl = urlBuilder.build();

        Request.Builder requestBuilder = new Request.Builder().url(httpUrl).get();

        if (reqHeaders != null && !reqHeaders.isEmpty()) {
            reqHeaders.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();
        Call call = client.newCall(request);

        try {
            log.debug("go to execute get, curl={}", buildCurlCommand(request, null));
            Response response = call.execute();
            return handleResponse(response, request, respBodyType);
        } catch (InterruptedIOException e) {
            log.error("fail to execute get clause timeout, curl={}", buildCurlCommand(request, null), e);
            throw new TimeoutException();
        } catch (Exception e) {
            log.error("fail to execute get, curl={}", buildCurlCommand(request, null), e);
            throw new HttpException("远程请求失败");
        }
    }

    private static <T> T handleResponse(Response resp, Request request, TypeReference<T> typeReference) {
        try (ResponseBody respBody = resp.body()) {
            if (!resp.isSuccessful()) {
                String errorBody = respBody != null ? respBody.string() : "";
                String requestBodyJson = extractRequestBody(request);
                log.error("fail to handle resp clause not 200 code, code={}, curl={}, respBody={}", resp.code(),
                        buildCurlCommand(request, requestBodyJson), errorBody);
                throw new Not200Exception(resp.code());
            }

            if (respBody == null) {
                String requestBodyJson = extractRequestBody(request);
                log.error("fail to handle resp clause null resp body, curl={}",
                        buildCurlCommand(request, requestBodyJson));
                throw new HttpException("远程请求失败");
            }

            String responseJson = respBody.string();
            log.debug("success to to handle resp, curl={}, respBody={}",
                    buildCurlCommand(request, extractRequestBody(request)), responseJson);

            if (typeReference == null) {
                return (T) JsonUtils.toTree(responseJson);
            } else {
                return JsonUtils.toParameterizedObject(responseJson, typeReference);
            }
        } catch (Exception e) {
            String requestBodyJson = extractRequestBody(request);
            log.error("fail to handle resp, curl={}", buildCurlCommand(request, requestBodyJson), e);
            throw new HttpException("远程请求失败");
        }
    }

    private static String buildCurlCommand(Request request, String requestBodyJson) {
        StringBuilder curl = new StringBuilder("curl -X ").append(request.method());

        // 添加URL
        String url = request.url().toString();
        String escapedUrl = url.replace("'", "'\\''");
        curl.append(" '").append(escapedUrl).append("'");

        // 添加请求头
        request.headers().forEach(header -> {
            String headerName = header.getFirst();
            String headerValue = header.getSecond();
            // 转义单引号
            String escapedName = headerName.replace("'", "'\\''");
            String escapedValue = headerValue.replace("'", "'\\''");
            curl.append(" -H '").append(escapedName).append(": ").append(escapedValue).append("'");
        });

        // 添加请求体（POST/PUT等方法）
        RequestBody body = request.body();
        if (body != null) {
            // 检查是否是 FormBody（form data）
            if (body instanceof FormBody) {
                FormBody formBody = (FormBody) body;
                for (int i = 0; i < formBody.size(); i++) {
                    String name = formBody.name(i);
                    String value = formBody.value(i);
                    // 转义单引号
                    String escapedName = name.replace("'", "'\\''");
                    String escapedValue = value.replace("'", "'\\''");
                    curl.append(" -F '").append(escapedName).append("=").append(escapedValue).append("'");
                }
            } else {
                // JSON 或其他格式的请求体
                if (requestBodyJson != null && !requestBodyJson.isEmpty()) {
                    // 转义单引号，将单引号替换为 '\''
                    String escapedBody = requestBodyJson.replace("'", "'\\''");
                    curl.append(" -d '").append(escapedBody).append("'");
                } else {
                    // 尝试从Request中提取请求体
                    try {
                        Buffer buffer = new Buffer();
                        body.writeTo(buffer);
                        String bodyStr = buffer.readUtf8();
                        if (!bodyStr.isEmpty()) {
                            String escapedBody = bodyStr.replace("'", "'\\''");
                            curl.append(" -d '").append(escapedBody).append("'");
                        }
                    } catch (IOException e) {
                        // 忽略提取失败的情况
                    }
                }
            }
        }

        return curl.toString();
    }

    private static String extractRequestBody(Request request) {
        RequestBody body = request.body();
        if (body == null) {
            return null;
        }
        try {
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            return buffer.readUtf8();
        } catch (IOException e) {
            return null;
        }
    }

    private static OkHttpClient createClient(long timeoutSec) {
        if (timeoutSec == DEFAULT_TIMEOUT_SEC) {
            return DEFAULT_CLIENT;
        }
        return new OkHttpClient.Builder().callTimeout(timeoutSec, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(20, 300, TimeUnit.SECONDS)).build();
    }

    public static class HttpException extends Allison1875Exception {

        private static final long serialVersionUID = 7393159341416075064L;

        public HttpException() {
            this("远程请求失败");
        }

        public HttpException(String message) {
            super(message);
        }

    }

    public static class TimeoutException extends HttpException {

        private static final long serialVersionUID = -6300167740026792740L;

        public TimeoutException() {
            super("远程请求超时");
        }

    }

    public static class Not200Exception extends HttpException {

        private static final long serialVersionUID = 7393159341416075064L;

        @Getter
        private final int httpCode;

        public Not200Exception(int httpCode) {
            this.httpCode = httpCode;
        }

    }

}
