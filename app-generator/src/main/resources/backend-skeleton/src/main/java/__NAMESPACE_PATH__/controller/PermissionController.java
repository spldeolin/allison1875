package __NAMESPACE__.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import __NAMESPACE__.common.RequestResult;
import __NAMESPACE__.dto.resp.PermissionGroupResp;
import __NAMESPACE__.dto.resp.PermissionResp;
import __NAMESPACE__.enums.PermissionEnum;

@RestController
@RequestMapping("/api/v1/permission")
public class PermissionController {

    @PostMapping("listPermissions")
    public RequestResult<List<PermissionGroupResp>> listPermissions() {
        List<PermissionGroupResp> result = new ArrayList<>();
        for (PermissionEnum.Group group : PermissionEnum.Group.values()) {
            PermissionGroupResp groupResp = new PermissionGroupResp();
            groupResp.setGroupCode(group.getCode());
            groupResp.setGroupTitle(group.getTitle());
            groupResp.setPermissions(
                    Arrays.stream(PermissionEnum.values())
                            .filter(p -> p.getGroup() == group)
                            .map(p -> {
                                PermissionResp resp = new PermissionResp();
                                resp.setCode(p.getCode());
                                resp.setTitle(p.getTitle());
                                resp.setBaseOn(
                                        p.getBaseOn() != null ? p.getBaseOn().stream().map(PermissionEnum::getCode)
                                                .collect(Collectors.toList()) : null);
                                return resp;
                            })
                            .collect(Collectors.toList())
            );
            result.add(groupResp);
        }
        return RequestResult.success(result);
    }

}
