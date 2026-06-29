package com.spldeolin.allison1875.common.config;

import java.io.File;
import java.util.List;
import java.util.Objects;
import com.spldeolin.allison1875.common.enums.FlushToEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Deolin 2026-06-29
 */
class ConfigTest {

    private File fixture(String name) {
        return new File(Objects.requireNonNull(
                getClass().getClassLoader().getResource("config/" + name)).getFile());
    }

    @Test
    void shouldDeserializeFullConfig() {
        Config config = Config.fromYaml(fixture("full-config.yml"));
        assertEquals("Test Author", config.getAuthor());
        assertTrue(config.getIsDataModelWithoutLombok());
        assertFalse(config.getEnableNoModifyAnnounce());
        assertTrue(config.getEnableJavaxMoveToJakarta());
        assertTrue(config.getEnableOneService());
        assertEquals("jdbc:mysql://localhost:3306/test", config.getJdbcUrl());
        assertEquals("root", config.getUserName());
        assertEquals("123456", config.getPassword());
        assertEquals("test_db", config.getSchema());
        assertFalse(config.getEnableGenerateDesign());
        assertFalse(config.getIsEntityEndWithEntity());
        assertEquals("DTO", config.getWholeDTONamePostfix());
        assertEquals("/api", config.getGlobalUrlPrefix());
        assertEquals("getValue", config.getGetEnumCodeMethodName());
        assertEquals("getLabel", config.getGetEnumTitleMethodName());
        assertEquals(1, config.getDomains().size());
        DomainConfig dc = config.getDomains().get(0);
        assertEquals("order", dc.getName());
        assertEquals("/tmp/order", dc.getControllerModule());
        assertEquals("com.example.controller", dc.getControllerPackage());
    }

    @Test
    void shouldApplyDefaults() {
        Config config = Config.fromYaml(fixture("minimal-config.yml"));
        assertEquals("Allison 1875", config.getAuthor());
        assertFalse(config.getIsDataModelWithoutLombok());
        assertTrue(config.getEnableNoModifyAnnounce());
        assertFalse(config.getEnableJavaxMoveToJakarta());
        assertFalse(config.getEnableOneService());
        assertTrue(config.getEnableGenerateDesign());
        assertTrue(config.getIsEntityEndWithEntity());
        assertEquals("WholeDTO", config.getWholeDTONamePostfix());
        assertEquals("", config.getGlobalUrlPrefix());
        assertEquals(List.of(FlushToEnum.MARKDOWN), config.getFlushTo());
        assertEquals(new File("api-docs"), config.getMarkdownDir());
        assertEquals("doc-analyzer", config.getShowdocBaseCatName());
        assertEquals(new File("api-dsls"), config.getDslDir());
        assertFalse(config.getSingleEndpointPerMarkdown());
        assertEquals("getCode", config.getGetEnumCodeMethodName());
        assertEquals("getTitle", config.getGetEnumTitleMethodName());
        assertEquals(new File("./app.yml"), config.getAppDslPath());
        assertEquals(new File("./output"), config.getAppGeneratorOutputDir());
        assertEquals(new File("./forms.yml"), config.getDslPath());
        assertNotNull(config.getCodeSnippet());
        assertEquals("/api/v1/${formName}", config.getCodeSnippet().getControllerRequestMapping());
        assertEquals("java.lang.RuntimeException", config.getCodeSnippet().getBizExceptionQualifier());
        DomainConfig dc = config.getDomains().get(0);
        assertEquals(List.of(new File("src/main/resources/mapper")), dc.getMapperXmlDirs());
    }

    @Test
    void shouldFailWhenDomainsEmpty() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("empty-domains.yml")));
        assertTrue(ex.getMessage().contains("domains must not be empty"));
    }

    @Test
    void shouldFailWhenJdbcUrlWithoutCredentials() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("invalid-jdbc.yml")));
        assertTrue(ex.getMessage().contains("userName must not be empty"));
        assertTrue(ex.getMessage().contains("password must not be empty"));
        assertTrue(ex.getMessage().contains("schema must not be empty"));
    }

    @Test
    void shouldFailWhenYapiFlushToWithoutUrl() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("invalid-yapi.yml")));
        assertTrue(ex.getMessage().contains("yapiUrl must not be null"));
        assertTrue(ex.getMessage().contains("yapiToken must not be null"));
    }

    @Test
    void shouldFailWhenCodeSnippetPartiallyFilled() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("invalid-code-snippet.yml")));
        assertTrue(ex.getMessage().contains("requestResultTypeDeclaration must not be empty"));
        assertTrue(ex.getMessage().contains("requestResultSuccessNoData must not be empty"));
        assertTrue(ex.getMessage().contains("requestResultSuccessWithData must not be empty"));
    }

    @Test
    void shouldDeriveConfigWithToBuilder() {
        Config original = Config.fromYaml(fixture("minimal-config.yml"));
        Config derived = original.toBuilder()
                .ddl("CREATE TABLE t (id BIGINT)")
                .jdbcUrl(null)
                .enableGenerateDesign(true)
                .build();
        assertNull(original.getDdl());
        assertEquals("CREATE TABLE t (id BIGINT)", derived.getDdl());
        assertNull(derived.getJdbcUrl());
        assertTrue(derived.getEnableGenerateDesign());
        assertEquals("Allison 1875", derived.getAuthor());
    }

    @Test
    void shouldValidateDomainConfigRequiredFields() {
        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> Config.fromYaml(fixture("invalid-domain-fields.yml")));
        assertTrue(ex.getMessage().contains("controllerPackage must not be empty"));
        assertTrue(ex.getMessage().contains("dtoModule must not be empty"));
    }
}
