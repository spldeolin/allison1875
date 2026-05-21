package com.spldeolin.allison1875.cli.it.startransformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * star-transformer 集成测试：最小可用链（cft only）。
 *
 * <p>验证单方法单链 StarSchema.cft(TOrderDesign.id, orderId).over() 的完整转换流程：
 * 1. 生成 WholeDTO 文件 TOrderWholeDTO.java，包含 TOrderEntity tOrder 字段
 * 2. Service 中 StarSchema 链被替换为标准查询/装配代码
 * 3. 原 DSL 语句消失
 *
 * @author Deolin 2026-05-20
 */
public class CftOnlyItTest extends StarTransformerItBaseTest {

    @Test
    void test() throws IOException {
        runStarTransformer("cft-only");

        // ========== 1. WholeDTO 文件验证 ==========
        File wholeDTOFile = new File(basedir, "src/main/java/com/example/dto/whole/TOrderWholeDTO.java");
        assertTrue(wholeDTOFile.exists(), "TOrderWholeDTO.java should exist");
        String wholeDTOContent = new String(Files.readAllBytes(wholeDTOFile.toPath()), StandardCharsets.UTF_8);

        // WholeDTO 类名
        assertTrue(wholeDTOContent.contains("class TOrderWholeDTO"), "WholeDTO should have class name TOrderWholeDTO");

        // 字段：TOrderEntity tOrder（cft only 只有事实表字段）
        assertTrue(wholeDTOContent.contains("TOrderEntity tOrder"),
                "WholeDTO should contain TOrderEntity tOrder field");

        // ========== 2. Service 文件验证 ==========
        File serviceFile = new File(basedir, "src/main/java/com/example/service/OrderAssembleService.java");
        assertTrue(serviceFile.exists(), "OrderAssembleService.java should exist");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);

        // StarSchema DSL 链已被替换移除
        assertFalse(serviceContent.contains("StarSchema."), "StarSchema chain should be replaced and removed");
        assertFalse(serviceContent.contains(".over()"), ".over() call should be removed");

        // 替换后的代码：WholeDTO 声明
        assertTrue(serviceContent.contains("TOrderWholeDTO whole = new TOrderWholeDTO();"),
                "Should have whole DTO instantiation");

        // 替换后的代码：事实表查询
        assertTrue(
                serviceContent.contains("TOrderEntity tOrder = TOrderDesign.query().byForced().id.eq(orderId).one();"),
                "Should have cft query via Design");

        // 替换后的代码：装配 setter
        assertTrue(serviceContent.contains("whole.setTOrder(tOrder);"), "Should have whole.setTOrder setter call");
    }

}
