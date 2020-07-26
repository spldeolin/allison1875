package com.spldeolin.allison1875.docanalyzer.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.http.HttpStatus;
import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * HTTP请求工具类
 * <pre>
 * 支持：
 * RESTful GET请求
 * RESTful POST请求
 * 提交form表单
 * 根据URL获取图片
 * </pre>
 *
 * @author Deolin
 */
@Log4j2
public class HttpUtils {

    private static final OkHttpClient client = new OkHttpClient();

    private HttpUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 发送一个GET请求，获取JSON
     * <pre>
     * e.g.: Https.getJson("https://api.live.bilibili.com/ajax/feed/count");
     * response不是JSON时，将会抛出异常
     * </pre>
     */
    public static String get(String url) {
        log.info("发送GET请求 {}", url);
        try {
            Request request = buildGetRequest(url);
            Response response = doRequest(request);

            return ensureJsonAndGetBody(response);
        } catch (IOException e) {
            log.error("url={}", url, e);
            throw new HttpException(e);
        }
    }

    /**
     * 发送一个GET请求，获取图片
     * <pre>
     * e.g.: Https.getImage("https://spldeolin.com/images/favicon.png");
     * response不是图片时，将会抛出异常
     * </pre>
     */
    public static BufferedImage getAsImage(String url) {
        log.info("发送GET请求 {}", url);
        try {
            Request request = buildGetRequest(url);

            Response response = doRequest(request);
            if (!Strings.nullToEmpty(response.header("Content-Type")).startsWith("image/")) {
                throw new RuntimeException("this url is not for a image.");
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new HttpException("image absent.");
            }
            return ImageIO.read(body.byteStream());
        } catch (IOException e) {
            log.error("url={}", url, e);
            throw new HttpException(e);
        }
    }

    /**
     * 发送一个POST请求，获取JSON，请求Body是JSON
     * <pre>
     * e.g.: Https.post("http://spldeolin.com/post/start", Jsons.toJson(userDTO));
     * </pre>
     */
    public static String postJson(String url, String bodyJson) {
        try {
            Request request = buildJsonPostRequest(url, bodyJson);
            Response response = doRequest(request);

            return ensureJsonAndGetBody(response);
        } catch (IOException e) {
            log.error("url={}, bodyJson={}", url, bodyJson, e);
            throw new HttpException(e);
        }
    }

    /**
     * 发送一个POST请求，请求Body的格式是Form表单
     * <pre>
     * e.g.: Https.post("http://spldeolin.com/post/like", userDTO);
     * </pre>
     */
    public static String postForm(String url, Object object) {
        FormBody.Builder form = new FormBody.Builder();
        try {
            if (object instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) object;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object key = entry.getKey();
                    if (key == null) {
                        continue;
                    }
                    Object value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    form.add(key.toString(), value.toString());
                }
            } else {
                for (Field field : object.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value != null) {
                        form.add(field.getName(), value.toString());
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            log.error("url={}, object={}", url, object, e);
            throw new HttpException(e);
        }

        try {
            Request request = new Request.Builder().url(url).post(form.build()).build();
            Response response = doRequest(request);

            return ensureJsonAndGetBody(response);
        } catch (IOException e) {
            log.error("url={}, object={}", url, object, e);
            throw new HttpException(e);
        }
    }

    /**
     * 为GET请求，构造request对象
     */
    private static Request buildGetRequest(String url) {
        return new Request.Builder().url(url).build();
    }

    /**
     * 为POST请求，构造body是JSON的request对象
     */
    private static Request buildJsonPostRequest(String url, String json) {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        return new Request.Builder().url(url).post(body).build();
    }

    /**
     * 确保response的body为JSON，并返回body的String形式
     */
    private static String ensureJsonAndGetBody(Response response) throws IOException {
        if (!Strings.nullToEmpty(response.header("Content-Type")).startsWith("application/json")) {
            throw new HttpException("response Content-Type is not json.");
        }
        ResponseBody body = response.body();
        if (body == null) {
            throw new HttpException("response body empty.");
        }
        return body.string();
    }

    /**
     * 发送请求，获取response，非200则重试5次，第5次后依然非200则抛出异常
     */
    private static Response doRequest(Request request) throws IOException {
        // 发送请求，获取response，非200则重试5次
        Response response = null;
        for (int i = 0; i < 5; i++) {
            response = client.newCall(request).execute();
            if (HttpStatus.OK.value() == response.code()) {
                break;
            } else {
                // 5次后依然非200则抛出异常
                if (i == 5 - 1) {
                    throw new HttpException(response.message());
                }
            }
        }
        return response;
    }

}