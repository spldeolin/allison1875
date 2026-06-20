package __NAMESPACE__.dto.req;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-21
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ListRolesReq {

    /**
     * 按业务主键列表过滤，null或empty代表无需过滤
     */
    List<String> roleCode;

    /**
     * 按“角色名称”模糊匹配过滤，null或empty代表无需过滤
     */
    String roleName;

    /**
     * 按“角色描述”模糊匹配过滤，null或empty代表无需过滤
     */
    String description;

    /**
     * 按创建时间晚于该时间过滤，null代表无需过滤
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime createdAtStart;

    /**
     * 按创建时间早于该时间过滤，null代表无需过滤
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime createdAtEnd;

    /**
     * 分页页码
     */
    Integer pageNum = 1;

    /**
     * 分页条数
     */
    Integer pageSize = 10;
}
