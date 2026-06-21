package __NAMESPACE__.task;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.annotation.WebApiAuth;
import __NAMESPACE__.enums.PermissionEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2026-06-21
 */
@Component
@Slf4j
public class WebApiAuthRegistry {

    @Resource
    private ApplicationContext applicationContext;

    private Map<String, PermissionEnum[]> pathPermissions;

    @PostConstruct
    public void init() {
        pathPermissions = new HashMap<>();
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
        for (Object controller : controllers.values()) {
            Class<?> targetClass = controller.getClass();
            // For CGLIB proxies, get the actual class
            if (targetClass.getName().contains("$$")) {
                targetClass = targetClass.getSuperclass();
            }

            RequestMapping classMapping = targetClass.getAnnotation(RequestMapping.class);
            if (classMapping == null) {
                continue;
            }
            String prefix = classMapping.value().length > 0 ? classMapping.value()[0] : "";

            for (Method method : targetClass.getDeclaredMethods()) {
                WebApiAuth webApiAuth = method.getAnnotation(WebApiAuth.class);
                if (webApiAuth == null) {
                    continue;
                }
                PostMapping postMapping = method.getAnnotation(PostMapping.class);
                if (postMapping == null) {
                    continue;
                }
                String methodPath = postMapping.value().length > 0 ? postMapping.value()[0] : "";
                String fullPath = (prefix + "/" + methodPath).replaceAll("/+", "/");
                pathPermissions.put(fullPath, webApiAuth.value());
            }
        }
        log.info("WebApiAuthRegistry initialized with {} path permissions", pathPermissions.size());
    }

    public PermissionEnum[] getRequiredPermissions(String requestPath) {
        return pathPermissions.get(requestPath);
    }

}
