package __NAMESPACE__.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Deolin 2026-06-27
 */
@Getter
@AllArgsConstructor
public enum AuditOperationTypeEnum {

    LOGIN("login", "登录"),
    CHANGE_PASSWORD("changePassword", "修改密码"),
    LOGOUT("logout", "退出登录"),

    GRANT_ROLE("grantRole", "授予角色"),
    GRANT_PERMISSION("grantPermission", "授予权限"),
    CREATE_USER("createUser", "创建用户"),
    UPDATE_USER("updateUser", "编辑用户"),
    DELETE_USER("deleteUser", "删除用户"),

    CREATE_ROLE("createRole", "创建角色"),
    UPDATE_ROLE("updateRole", "编辑角色"),
    DELETE_ROLE("deleteRole", "删除角色"),

    // === 以下枚举项由 app-generator 生成，勿手动修改 ===
    ;

    @JsonValue
    private final String code;

    private final String title;

    public static boolean valid(String code) {
        return Arrays.stream(values()).anyMatch(anEnum -> anEnum.getCode().equals(code));
    }

    @JsonCreator
    public static AuditOperationTypeEnum of(String code) {
        return Arrays.stream(values()).filter(anEnum -> anEnum.getCode().equals(code)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return code;
    }

}
