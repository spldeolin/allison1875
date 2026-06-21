package __NAMESPACE__.enums;

import java.util.List;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionEnum {

    LIST_USER("LIST_USER", "查看用户", Group.USER, null), CREATE_USER("CREATE_USER", "创建用户", Group.USER,
            Lists.newArrayList(LIST_USER)), UPDATE_USER("UPDATE_USER", "编辑用户", Group.USER,
            Lists.newArrayList(LIST_USER)), DELETE_USER("DELETE_USER", "删除用户", Group.USER,
            Lists.newArrayList(LIST_USER)),

    LIST_ROLE("LIST_ROLE", "查看角色", Group.ROLE, null), CREATE_ROLE("CREATE_ROLE", "创建角色", Group.ROLE,
            Lists.newArrayList(LIST_ROLE)), UPDATE_ROLE("UPDATE_ROLE", "编辑角色", Group.ROLE,
            Lists.newArrayList(LIST_ROLE)), DELETE_ROLE("DELETE_ROLE", "删除角色", Group.ROLE,
            Lists.newArrayList(LIST_ROLE)),

    GRANT_ROLE("GRANT_ROLE", "授予角色", Group.USER, Lists.newArrayList(LIST_USER, LIST_ROLE)), GRANT_PERMISSION(
            "GRANT_PERMISSION", "授予权限", Group.ROLE, Lists.newArrayList(LIST_ROLE)),

    // === 以下枚举项由 app-generator 生成，勿手动修改 ===
    ;

    private final String code;
    private final String title;
    private final Group group;

    private final List<PermissionEnum> baseOn;

    @Getter
    @AllArgsConstructor
    public enum Group {
        USER("USER", "用户管理"), ROLE("ROLE", "角色管理"),
        // === 由 app-generator 生成 ===
        ;

        private final String code;
        private final String title;
    }

}
