package __NAMESPACE__.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import __NAMESPACE__.util.JsonUtils;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonUtils.createObjectMapper();
    }

}
