package __NAMESPACE__.trace;

import javax.servlet.http.HttpServletRequest;

public interface HttpBodyReportFilterExclusion {

    boolean isExcluded(HttpServletRequest request);

}
