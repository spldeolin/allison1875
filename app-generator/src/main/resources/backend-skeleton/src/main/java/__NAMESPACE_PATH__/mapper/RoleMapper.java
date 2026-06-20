package __NAMESPACE__.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.dto.param.QueryRoleParam;
import __NAMESPACE__.entity.RoleEntity;

/**
 * 角色
 * <p>role
 *
 * @author Deolin 2026-06-21
 */
public interface RoleMapper {

    /**
     * 插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int insert(RoleEntity entity);

    /**
     * 批量插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsert(@Param("entities") List<RoleEntity> entities);

    /**
     * 批量插入，为null的属性会被作为null插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsertEvenNull(@Param("entities") List<RoleEntity> entities);

    /**
     * 根据ID更新数据，忽略值为null的属性
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateById(RoleEntity entity);

    /**
     * 根据ID更新数据，为null属性对应的字段会被更新为null
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateByIdEvenNull(RoleEntity entity);

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
    RoleEntity queryById(Long id);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<RoleEntity> queryByIds(@Param("ids") List<Long> ids);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("id")
    Map<Long, RoleEntity> queryByIdsEachId(@Param("ids") List<Long> ids);

    /**
     * 根据“业务主键”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    RoleEntity queryByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据“业务主键”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<RoleEntity> queryByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("roleCode")
    Map<String, RoleEntity> queryByRoleCodesEachRoleCode(@Param("roleCodes") List<String> roleCodes);

    /**
     * 根据“角色名称”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    RoleEntity queryByRoleName(@Param("roleName") String roleName);

    /**
     * 根据“角色名称”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByRoleName(@Param("roleName") String roleName);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<RoleEntity> queryByRoleNames(@Param("roleNames") List<String> roleNames);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("roleName")
    Map<String, RoleEntity> queryByRoleNamesEachRoleName(@Param("roleNames") List<String> roleNames);

    RoleEntity queryRole(@Param("roleCode") String roleCode, @Param("roleName") String roleName);

    long countRole(QueryRoleParam queryRoleParam);

    List<RoleEntity> queryRoleEx(QueryRoleParam queryRoleParam);

    int deleteRole(@Param("roleCode") List<String> roleCode);
}
