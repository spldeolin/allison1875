package __NAMESPACE__.dto.req;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-06-07
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ListUsersReq {

    /**
     * 按业务主键列表过滤，null或empty代表无需过滤
     */
    List<String> userCode;

    /**
     * 按“用户名”模糊匹配过滤，null或empty代表无需过滤
     */
    String username;

    /**
     * 按“用户昵称”模糊匹配过滤，null或empty代表无需过滤
     */
    String nickName;

    /**
     * 按“最后登录时间”晚于该时间过滤，null代表无需过滤
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime lastLoginAtStart;

    /**
     * 按“最后登录时间”早于该时间过滤，null代表无需过滤
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    LocalDateTime lastLoginAtEnd;

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
