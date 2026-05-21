package com.spldeolin.allison1875.cli.it.startransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * star-transformer 集成测试：非 StarSchema 链不应被改写（负向验证）。
 *
 * <p>验证 detectStarChains → finalNameExprRecursively 的过滤逻辑：
 * 1. FieldAccessExpr scope 的 .over() 链（FakeChain.INSTANCE.foo().over()）保留不动
 * 2. NameExpr scope 解析为非 StarSchema 的 .over() 链（fakeChain.foo().over()）保留不动
 * 3. 正常 StarSchema 链正确展开
 * 4. 不生成任何多余的 WholeDTO
 *
 * @author Deolin 2026-05-20
 */
public class NotStarChainNoTransformItTest extends StarTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runStarTransformer("not-star-chain");

        // ========== 1. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderAssembleService.java");
        assertTrue(serviceFile.exists(), "OrderAssembleService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        // 语句1: FieldAccessExpr scope → 保留不动（含分号精确匹配，避免误匹配Javadoc）
        assertTrue(serviceContent.contains("FakeChain.INSTANCE.foo().over();"),
                "FakeChain.INSTANCE.foo().over() should remain untouched (FieldAccessExpr scope)");

        // 语句2: NameExpr scope 解析为非 StarSchema → 保留不动
        assertTrue(serviceContent.contains("fakeChain.foo().over();"),
                "fakeChain.foo().over() should remain untouched (non-StarSchema NameExpr)");

        // 语句3: StarSchema 链已被替换移除
        // 注：FakeChain.INSTANCE.foo().over() 和 fakeChain.foo().over() 中仍含 .over()，
        // 因此仅断言 StarSchema.cft 消失，不能断言 .over() 消失
        assertFalse(serviceContent.contains("StarSchema.cft"),
                "StarSchema.cft(...) chain should be replaced and removed");

        // 替换后的代码：WholeDTO 声明
        assertTrue(serviceContent.contains("TOrderWholeDTO whole = new TOrderWholeDTO();"),
                "Should have whole DTO instantiation");

        // 替换后的代码：事实表查询
        assertTrue(
                serviceContent.contains("TOrderEntity tOrder = TOrderDesign.query().byForced().id.eq(orderId).one();"),
                "Should have cft query via Design");

        // 替换后的代码：装配 setter
        assertTrue(serviceContent.contains("whole.setTOrder(tOrder);"), "Should have whole.setTOrder setter call");

        // ========== 2. WholeDTO 文件验证 ==========
        File wholeDTOFile = new File(basedir, "src/main/java/com/example/dto/whole/TOrderWholeDTO.java");
        assertTrue(wholeDTOFile.exists(), "TOrderWholeDTO.java should exist");

        // 不应生成多余的 WholeDTO 文件（whole 目录下应仅有 TOrderWholeDTO.java）
        File wholeDTODir = new File(basedir, "src/main/java/com/example/dto/whole");
        File[] wholeDTOFiles = wholeDTODir.listFiles((dir, name) -> name.endsWith("WholeDTO.java"));
        assertTrue(wholeDTOFiles != null && wholeDTOFiles.length == 1,
                "Only one WholeDTO file should be generated, but found: " + (wholeDTOFiles == null ? "null"
                        : wholeDTOFiles.length));
    }

}
