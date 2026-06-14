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
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpBodyReportFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        log.info("requestArrived-{}-{}", request.getRequestURL(), MDC.get("traceId"));
        long start = System.currentTimeMillis();

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            log.info("requestLeft {} {} {} {} {} {}", request.getMethod(), request.getRequestURI(), MDC.get("traceId"),
                    System.currentTimeMillis() - start, getRawRequestBody(wrappedRequest),
                    getRawResponseBody(wrappedResponse));
        }
    }

    private String getRawRequestBody(ContentCachingRequestWrapper wrappedRequest) {
        String contentType = wrappedRequest.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType == null) {
            return "";
        }
        if (!contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return "<REQUEST BODY IS NOT A JSON>";
        }
        try {
            String encoding = wrappedRequest.getCharacterEncoding();
            String result = IOUtils.toString(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8.toString());
            if (StringUtils.isEmpty(result)) {
                result = IOUtils.toString(wrappedRequest.getInputStream(), encoding);
            }
            return result;
        } catch (IOException e) {
            log.error("读取request body失败", e);
            return "";
        }
    }

    private String getRawResponseBody(ContentCachingResponseWrapper wrappedResponse) {
        try {
            wrappedResponse.copyBodyToResponse();
            String contentType = wrappedResponse.getHeader(HttpHeaders.CONTENT_TYPE);
            if (contentType != null && !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                return "<RESPONSE BODY NOT A JSON>";
            }
            return IOUtils.toString(wrappedResponse.getContentInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取response body失败", e);
            return "";
        }
    }

}
