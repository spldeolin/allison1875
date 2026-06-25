package __NAMESPACE__.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import __NAMESPACE__.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TraceFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "x-trace-id";

    private static final int MAX_BODY_LENGTH = 2048;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String spanId = UuidUtils.generateShort().substring(0, 8);
        String incomingTraceId = request.getHeader(TRACE_HEADER);
        if (StringUtils.isNotBlank(incomingTraceId)) {
            MDC.put("traceId", incomingTraceId + "," + spanId);
        } else {
            MDC.put("traceId", spanId);
        }
        try {
            log.info("request arrived, api={}", request.getRequestURI());
            long start = System.currentTimeMillis();

            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

            try {
                filterChain.doFilter(wrappedRequest, wrappedResponse);
            } finally {
                log.info("request left, api={}, elapsed={}ms, req={}, resp={}", request.getRequestURI(),
                        System.currentTimeMillis() - start, getRawRequestBody(wrappedRequest),
                        getRawResponseBody(wrappedResponse));
            }
        } catch (Exception e) {
            log.error("request failed, api={}", request.getRequestURI(), e);
            throw e;
        } finally {
            MDC.remove("traceId");
        }
    }

    private String getRawRequestBody(ContentCachingRequestWrapper wrappedRequest) {
        String contentType = wrappedRequest.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return "";
        }
        try {
            byte[] bytes = wrappedRequest.getContentAsByteArray();
            if (bytes.length == 0) {
                bytes = IOUtils.toByteArray(wrappedRequest.getInputStream());
            }
            if (bytes.length == 0) {
                return "";
            }
            String body = new String(bytes, StandardCharsets.UTF_8);
            return truncate(body);
        } catch (IOException e) {
            log.error("failed to read request body", e);
            return "";
        }
    }

    private String getRawResponseBody(ContentCachingResponseWrapper wrappedResponse) {
        try {
            byte[] bytes = wrappedResponse.getContentAsByteArray();
            wrappedResponse.copyBodyToResponse();
            if (bytes.length == 0) {
                return "";
            }
            String contentType = wrappedResponse.getContentType();
            if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                return "";
            }
            return truncate(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("failed to read response body", e);
            return "";
        }
    }

    private String truncate(String body) {
        if (body.length() <= MAX_BODY_LENGTH) {
            return body;
        }
        return body.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
    }

}
