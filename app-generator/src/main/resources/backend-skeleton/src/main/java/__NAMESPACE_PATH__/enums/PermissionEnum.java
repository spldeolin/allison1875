package __NAMESPACE__.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionEnum {

    // === 枚举项由 app-generator 生成，勿手动修改 ===
    ;

    private final String code;
    private final String title;
    private final Group group;
    private final PermissionEnum baseOn;

    @Getter
    @AllArgsConstructor
    public enum Group {
        // === 由 app-generator 生成 ===
        ;

        private final String code;
        private final String title;
    }

}
