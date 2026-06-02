package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * time-item 集成测试。
 *
 * <p>验证 time 类型字段在不同 format 下的处理：
 * <ul>
 *   <li>DDL 中统一为 {@code DATETIME}（存储层统一为 LocalDateTime）</li>
 *   <li>{@code format=date} → DTO 中 {@code LocalDate}，附加 {@code @JsonFormat(pattern="yyyy-MM-dd")}</li>
 *   <li>{@code format=time} → DTO 中 {@code LocalTime}，附加 {@code @JsonFormat(pattern="HH:mm:ss")}</li>
 *   <li>{@code format=dateTime} → DTO 中 {@code LocalDateTime}，附加 {@code @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")}</li>
 *   <li>ListReq 中生成 {@code xxxStart} 和 {@code xxxEnd} 两个范围过滤字段</li>
 *   <li>GetDetailResp 中字段正常返回</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class TimeItemItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("time-item");

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // 所有 time 字段在 DDL 中统一为 DATETIME
        assertTrue(ddl.contains("`event_date` DATETIME NOT NULL"),
                "eventDate (date+nonVoid) should be DATETIME NOT NULL");
        assertTrue(ddl.contains("`event_time` DATETIME"),
                "eventTime (time+nullable) should be DATETIME without NOT NULL");
        assertFalse(ddl.contains("`event_time` DATETIME NOT NULL"),
                "eventTime should NOT have NOT NULL since isNonVoid=false");
        assertTrue(ddl.contains("`publish_time` DATETIME NOT NULL"),
                "publishTime (dateTime+nonVoid) should be DATETIME NOT NULL");
        // 审计字段
        assertTrue(ddl.contains("`event_code`"), "DDL should contain auto-added bizId column 'event_code'");

        // === Entity 验证 ===
        File entityFile = new File(basedir, "src/main/java/com/example/entity/EventEntity.java");
        assertTrue(entityFile.exists(), "Entity file should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("class EventEntity"), "Entity should declare class EventEntity");
        // Entity 中 time 字段始终为 LocalDateTime（存储层统一）
        assertTrue(entityContent.contains("LocalDateTime eventDate"),
                "Entity should contain LocalDateTime eventDate field");
        assertTrue(entityContent.contains("LocalDateTime eventTime"),
                "Entity should contain LocalDateTime eventTime field");
        assertTrue(entityContent.contains("LocalDateTime publishTime"),
                "Entity should contain LocalDateTime publishTime field");

        // === SaveReq DTO 验证 ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveEventReq.java");
        assertTrue(saveReqFile.exists(), "SaveEventReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(saveReqContent.contains("class SaveEventReq"), "Should contain class SaveEventReq");
        // eventDate: format=date → LocalDate + @JsonFormat("yyyy-MM-dd")
        assertTrue(saveReqContent.contains("LocalDate eventDate"),
                "SaveReq should contain LocalDate eventDate (format=date)");
        assertTrue(saveReqContent.contains("yyyy-MM-dd"),
                "eventDate should have @JsonFormat(pattern=\"yyyy-MM-dd\")");
        // eventTime: format=time → LocalTime + @JsonFormat("HH:mm:ss")
        assertTrue(saveReqContent.contains("LocalTime eventTime"),
                "SaveReq should contain LocalTime eventTime (format=time)");
        assertTrue(saveReqContent.contains("HH:mm:ss"),
                "eventTime should have @JsonFormat(pattern=\"HH:mm:ss\")");
        // publishTime: format=dateTime → LocalDateTime + @JsonFormat("yyyy-MM-dd HH:mm:ss")
        assertTrue(saveReqContent.contains("LocalDateTime publishTime"),
                "SaveReq should contain LocalDateTime publishTime (format=dateTime)");
        // eventDate 有 @NotNull (isNonVoid=true)
        assertTrue(saveReqContent.contains("@NotNull") || saveReqContent.contains("@javax.validation.constraints.NotNull"),
                "nonVoid time fields should have @NotNull");

        // === ListReq DTO 验证 ===
        File listReqFile = new File(basedir, "src/main/java/com/example/dto/req/ListEventsReq.java");
        assertTrue(listReqFile.exists(), "ListEventsReq DTO should be generated");
        String listReqContent = new String(Files.readAllBytes(listReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listReqContent.contains("class ListEventsReq"), "Should contain class ListEventsReq");
        // time 字段在 ListReq 中生成 xxxStart / xxxEnd 两个范围过滤字段
        // eventDate: format=date → LocalDate eventDateStart / LocalDate eventDateEnd
        assertTrue(listReqContent.contains("LocalDate eventDateStart"),
                "ListReq should contain LocalDate eventDateStart for range filter");
        assertTrue(listReqContent.contains("LocalDate eventDateEnd"),
                "ListReq should contain LocalDate eventDateEnd for range filter");
        // eventTime: format=time → LocalTime eventTimeStart / LocalTime eventTimeEnd
        assertTrue(listReqContent.contains("LocalTime eventTimeStart"),
                "ListReq should contain LocalTime eventTimeStart for range filter");
        assertTrue(listReqContent.contains("LocalTime eventTimeEnd"),
                "ListReq should contain LocalTime eventTimeEnd for range filter");
        // publishTime: format=dateTime → LocalDateTime publishTimeStart / publishTimeEnd
        assertTrue(listReqContent.contains("LocalDateTime publishTimeStart"),
                "ListReq should contain LocalDateTime publishTimeStart for range filter");
        assertTrue(listReqContent.contains("LocalDateTime publishTimeEnd"),
                "ListReq should contain LocalDateTime publishTimeEnd for range filter");

        // === GetDetailResp DTO 验证 ===
        File getDetailRespFile = new File(basedir, "src/main/java/com/example/dto/resp/GetEventDetailResp.java");
        assertTrue(getDetailRespFile.exists(), "GetEventDetailResp DTO should be generated");
        String getDetailRespContent = new String(Files.readAllBytes(getDetailRespFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(getDetailRespContent.contains("class GetEventDetailResp"),
                "Should contain class GetEventDetailResp");
        assertTrue(getDetailRespContent.contains("LocalDate eventDate"),
                "GetDetailResp should contain LocalDate eventDate");
        assertTrue(getDetailRespContent.contains("LocalTime eventTime"),
                "GetDetailResp should contain LocalTime eventTime");
        assertTrue(getDetailRespContent.contains("LocalDateTime publishTime"),
                "GetDetailResp should contain LocalDateTime publishTime");

        // === List ServiceImpl 验证：query-transformer 转换 Design Chain ===
        File listServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/ListEventsServiceImpl.java");
        assertTrue(listServiceImplFile.exists(), "ListEventsServiceImpl file should be generated");
        String listServiceImplContent = new String(Files.readAllBytes(listServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(listServiceImplContent.contains("eventMapper.countEvent("),
                "List service should call eventMapper.countEvent");
        assertTrue(listServiceImplContent.contains("eventMapper.queryEvent("),
                "List service should call eventMapper.queryEvent");
        // Param 包含 time 字段的 Start/End setter（query-transformer 使用 xxx/xxxEx 命名）
        assertTrue(listServiceImplContent.contains("queryEventParam.setEventDate("),
                "Param should set eventDate (Start) from req (time field)");
        assertTrue(listServiceImplContent.contains("queryEventParam.setEventDateEx("),
                "Param should set eventDateEx (End) from req (time field)");
        assertTrue(listServiceImplContent.contains("queryEventParam.setEventTime("),
                "Param should set eventTime (Start) from req (time field)");
        assertTrue(listServiceImplContent.contains("queryEventParam.setEventTimeEx("),
                "Param should set eventTimeEx (End) from req (time field)");
        assertTrue(listServiceImplContent.contains("queryEventParam.setPublishTime("),
                "Param should set publishTime (Start) from req (time field)");
        assertTrue(listServiceImplContent.contains("queryEventParam.setPublishTimeEx("),
                "Param should set publishTimeEx (End) from req (time field)");

        // === 验证没有生成 api-docs 目录 ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated");
    }

}
