package __NAMESPACE__.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.entity.RolePermissionEntity;

public interface RolePermissionMapper {

    int batchInsert(@Param("entities") List<RolePermissionEntity> entities);

    int deleteByRoleId(@Param("roleId") Long roleId);

    List<String> queryPermissionCodesByRoleId(@Param("roleId") Long roleId);

    List<String> queryPermissionCodesByRoleIds(@Param("roleIds") List<Long> roleIds);

}
