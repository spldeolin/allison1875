package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * list-api 集成测试。
 *
 * <p>验证 List API 的完整生成：
 * <ul>
 *   <li>Controller 中生成 {@code listItems} handler（POST），入参 ListItemsReq，出参 PageResult&lt;ListItemsResp&gt;</li>
 *   <li>ListReq 包含各字段过滤条件 + 分页参数（pageNum/pageSize）</li>
 *   <li>各类型字段的过滤运算符：number/onOff/select → {@code List<T>}（IN），text → {@code String}（LIKE）</li>
 *   <li>ListResp 包含所有非 secret 字段，使用 {@code @P} 注解标记分页返回</li>
 *   <li>Service 中 Design Chain 已被 query-transformer 转换为 Mapper 调用（count + query + PageResult.of）</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class ListApiItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("list-api");

        // ============================================================
        // === Controller 验证 ===
        // ============================================================
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/ItemController.java");
        assertTrue(controllerFile.exists(), "ItemController file should be generated");
        String controllerContent = Files.readString(controllerFile.toPath());
        assertTrue(controllerContent.contains("class ItemController"), "Should declare class ItemController");
        assertTrue(controllerContent.contains("@RestController"), "Controller should have @RestController");
        // listItems handler
        assertTrue(controllerContent.contains("listItems"), "Controller should contain listItems handler");
        assertTrue(controllerContent.contains("ListItemsReq"), "Controller listItems should reference ListItemsReq");
        assertTrue(controllerContent.contains("PageResult"), "Controller listItems should return PageResult");
        assertTrue(controllerContent.contains("ListItemsResp"), "Controller should reference ListItemsResp");

        // ============================================================
        // === ListReq DTO 验证 ===
        // ============================================================
        File listReqFile = new File(basedir, "src/main/java/com/example/dto/req/ListItemsReq.java");
        assertTrue(listReqFile.exists(), "ListItemsReq DTO should be generated");
        String listReqContent = Files.readString(listReqFile.toPath());
        assertTrue(listReqContent.contains("class ListItemsReq"));

        // text 字段 → String 单值（LIKE 过滤）
        assertTrue(listReqContent.contains("String itemName"),
                "ListReq should contain String itemName for LIKE filter");
        // remark 也是 text（optional, nonVoid=false） → String
        assertTrue(listReqContent.contains("String remark"),
                "ListReq should contain String remark for LIKE filter");

        // number 字段（canBeDecimal=false → Long） → List<Long>（IN 过滤）
        assertTrue(listReqContent.contains("List<Long> quantity"),
                "ListReq should contain List<Long> quantity for IN filter");
        // number 字段（canBeDecimal=true → BigDecimal） → List<BigDecimal>（IN 过滤）
        assertTrue(listReqContent.contains("List<BigDecimal> unitPrice"),
                "ListReq should contain List<BigDecimal> unitPrice for IN filter");

        // select 字段 → List<Enum>（IN 过滤）
        assertTrue(listReqContent.contains("List<CategoryEnum> category"),
                "ListReq should contain List<CategoryEnum> category for IN filter");

        // onOff 字段 → List<Boolean>（IN 过滤）
        assertTrue(listReqContent.contains("List<Boolean> isActive"),
                "ListReq should contain List<Boolean> isActive for IN filter");

        // 分页参数
        assertTrue(listReqContent.contains("pageNum"), "ListReq should contain pageNum");
        assertTrue(listReqContent.contains("pageSize"), "ListReq should contain pageSize");
        assertTrue(
                listReqContent.contains("Integer pageNum") || listReqContent.contains("int pageNum"),
                "pageNum should be Integer/int type");
        assertTrue(
                listReqContent.contains("Integer pageSize") || listReqContent.contains("int pageSize"),
                "pageSize should be Integer/int type");

        // itemCode 作为业务主键出现在 ListReq 中，支持按 bizId 列表精确过滤
        assertTrue(listReqContent.contains("List<String> itemCode"),
                "ListReq should contain List<String> itemCode as bizId IN filter");

        // ============================================================
        // === ListResp DTO 验证 ===
        // ============================================================
        File listRespFile = new File(basedir, "src/main/java/com/example/dto/resp/ListItemsResp.java");
        assertTrue(listRespFile.exists(), "ListItemsResp DTO should be generated");
        String listRespContent = Files.readString(listRespFile.toPath());
        assertTrue(listRespContent.contains("class ListItemsResp"));
        // Controller 返回类型为 PageResult<ListItemsResp>（分页返回）
        assertTrue(controllerContent.contains("PageResult<ListItemsResp>"),
                "Controller listItems should return PageResult<ListItemsResp> for pagination");

        // 包含所有非 secret 字段
        assertTrue(listRespContent.contains("itemCode"), "ListResp should contain itemCode");
        assertTrue(listRespContent.contains("itemName"), "ListResp should contain itemName");
        assertTrue(listRespContent.contains("quantity"), "ListResp should contain quantity");
        assertTrue(listRespContent.contains("unitPrice"), "ListResp should contain unitPrice");
        assertTrue(listRespContent.contains("CategoryEnum category"), "ListResp should contain CategoryEnum category");
        assertTrue(listRespContent.contains("isActive"), "ListResp should contain isActive");
        assertTrue(listRespContent.contains("remark"), "ListResp should contain remark");
        assertTrue(listRespContent.contains("createdAt"), "ListResp should contain createdAt");
        assertTrue(listRespContent.contains("updatedAt"), "ListResp should contain updatedAt");

        // ============================================================
        // === List Service 接口验证 ===
        // ============================================================
        File listServiceFile = new File(basedir, "src/main/java/com/example/service/ListItemsService.java");
        assertTrue(listServiceFile.exists(), "ListItemsService interface should be generated");
        String listServiceContent = Files.readString(listServiceFile.toPath());
        assertTrue(listServiceContent.contains("interface ListItemsService"));
        assertTrue(listServiceContent.contains("listItems"), "ListItemsService should declare listItems method");

        // ============================================================
        // === List ServiceImpl 验证 — Design Chain 已被转换 ===
        // ============================================================
        File listServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/ListItemsServiceImpl.java");
        assertTrue(listServiceImplFile.exists(), "ListItemsServiceImpl should be generated");
        String listImplContent = Files.readString(listServiceImplFile.toPath());
        assertTrue(listImplContent.contains("class ListItemsServiceImpl"));
        assertTrue(listImplContent.contains("implements ListItemsService"));

        // Design Chain 已被 query-transformer 转换为 Mapper 调用
        // 计数查询
        assertTrue(listImplContent.contains("itemMapper.countItem("),
                "List service should call itemMapper.countItem for pagination count");
        // 数据查询
        assertTrue(listImplContent.contains("itemMapper.queryItem("),
                "List service should call itemMapper.queryItem for data query");

        // constructPageResult / constructEmptyPageResult
        assertTrue(listImplContent.contains("PageResult.of("),
                "List service should construct PageResult for non-empty results");

        // Param DTO 构建
        assertTrue(listImplContent.contains("QueryItemParam"),
                "List service should use QueryItemParam");

        // 分页逻辑：使用 req.getPageNum() 和 req.getPageSize()
        assertTrue(listImplContent.contains("getPageNum()"),
                "List service should call req.getPageNum() for offset calculation");
        assertTrue(listImplContent.contains("getPageSize()"),
                "List service should call req.getPageSize() for limit");
        assertTrue(listImplContent.contains("setOffset"),
                "List service should set offset on query param");
        assertTrue(listImplContent.contains("setLimit"),
                "List service should set limit on query param");

        // ============================================================
        // === Mapper 验证：count + query 方法 ===
        // ============================================================
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/ItemMapper.java");
        assertTrue(mapperFile.exists(), "ItemMapper interface should be generated");
        String mapperContent = Files.readString(mapperFile.toPath());
        assertTrue(mapperContent.contains("countItem"), "Mapper should contain countItem method");
        assertTrue(mapperContent.contains("queryItem"), "Mapper should contain queryItem method");

        // ============================================================
        // === Param DTO 验证 ===
        // ============================================================
        File paramFile = new File(basedir, "src/main/java/com/example/dto/param/QueryItemParam.java");
        assertTrue(paramFile.exists(), "QueryItemParam should be generated");
        String paramContent = Files.readString(paramFile.toPath());
        assertTrue(paramContent.contains("class QueryItemParam"), "Should declare class QueryItemParam");

        // ============================================================
        // === Mapper XML 验证 ===
        // ============================================================
        File xmlFile = new File(basedir, "src/main/resources/mapper/ItemMapper.xml");
        assertTrue(xmlFile.exists(), "ItemMapper XML should be generated");
        String xmlContent = Files.readString(xmlFile.toPath());
        assertTrue(xmlContent.contains("countItem"), "Mapper XML should contain countItem statement");
        assertTrue(xmlContent.contains("queryItem"), "Mapper XML should contain queryItem statement");

        // ============================================================
        // === 验证没有生成 api-docs 目录 ===
        // ============================================================
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated");
    }

}
