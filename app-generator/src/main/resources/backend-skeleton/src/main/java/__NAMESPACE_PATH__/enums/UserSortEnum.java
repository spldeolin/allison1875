package __NAMESPACE__.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户的排序字段
 *
 * @author Deolin 2026-06-27
 */
@Getter
@AllArgsConstructor
public enum UserSortEnum {

    USER_CODE("userCode", "按“业务主键”排序"),
    USERNAME("username", "按“用户名”排序"),
    NICK_NAME("nickName", "按“用户昵称”排序"),
    LAST_LOGIN_AT("lastLoginAt", "按“最后登录时间”排序"),
    CREATED_AT("createdAt", "按“创建时间”排序"),
    UPDATED_AT("updatedAt", "按“更新时间”排序"),
    CREATED_BY("createdBy", "按“创建人”排序"),
    UPDATED_BY("updatedBy", "按“最近更新人”排序");

    @JsonValue
    private final String code;

    private final String title;

    /**
     * 判断参数code是否是一个有效的枚举
     */
    public static boolean valid(String code) {
        return Arrays.stream(values()).anyMatch(anEnum -> anEnum.getCode().equals(code));
    }

    /**
     * 获取code对应的枚举
     */
    @JsonCreator
    public static UserSortEnum of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return code;
    }
}
