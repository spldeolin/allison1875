package __NAMESPACE__.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.entity.RolePermissionEntity;

public interface RolePermissionMapper {

    /**
     * 插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int insert(RolePermissionEntity entity);

    /**
     * 批量插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsert(@Param("entities") List<RolePermissionEntity> entities);

    /**
     * 批量插入，为null的属性会被作为null插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsertEvenNull(@Param("entities") List<RolePermissionEntity> entities);

    /**
     * 根据ID更新数据，忽略值为null的属性
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateById(RolePermissionEntity entity);

    /**
     * 根据ID删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteById(Long id);

    /**
     * 根据ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    RolePermissionEntity queryById(Long id);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<RolePermissionEntity> queryByIds(@Param("ids") List<Long> ids);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("id")
    Map<Long, RolePermissionEntity> queryByIdsEachId(@Param("ids") List<Long> ids);

    /**
     * 根据“角色ID”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<RolePermissionEntity> queryByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据“角色ID”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据“角色ID”、“权限编码”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    RolePermissionEntity queryByRoleIdPermissionCode(@Param("roleId") Long roleId,
            @Param("permissionCode") String permissionCode);

    /**
     * 根据“角色ID”、“权限编码”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByRoleIdPermissionCode(@Param("roleId") Long roleId, @Param("permissionCode") String permissionCode);

    List<String> queryPermissionCodesByRoleId(@Param("roleId") Long roleId);

    List<String> queryPermissionCodesByRoleIds(@Param("roleIds") List<Long> roleIds);

    List<RolePermissionEntity> queryByRoleIds(@Param("roleIds") List<Long> roleIds);

}
