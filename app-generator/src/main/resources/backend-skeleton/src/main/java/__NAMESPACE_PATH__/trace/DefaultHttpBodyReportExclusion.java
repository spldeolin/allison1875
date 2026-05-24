package __NAMESPACE__.trace;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class DefaultHttpBodyReportExclusion implements HttpBodyReportFilterExclusion {

    public boolean isExcluded(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/favicon.ico") || uri.equals("/");
    }

}
