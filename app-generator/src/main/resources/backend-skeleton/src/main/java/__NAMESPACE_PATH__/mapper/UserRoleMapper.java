package __NAMESPACE__.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.entity.UserRoleEntity;

public interface UserRoleMapper {

    /**
     * 插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int insert(UserRoleEntity entity);

    /**
     * 批量插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsert(@Param("entities") List<UserRoleEntity> entities);

    /**
     * 批量插入，为null的属性会被作为null插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsertEvenNull(@Param("entities") List<UserRoleEntity> entities);

    /**
     * 根据ID更新数据，忽略值为null的属性
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateById(UserRoleEntity entity);

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
    UserRoleEntity queryById(Long id);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<UserRoleEntity> queryByIds(@Param("ids") List<Long> ids);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("id")
    Map<Long, UserRoleEntity> queryByIdsEachId(@Param("ids") List<Long> ids);

    /**
     * 根据“用户ID”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<UserRoleEntity> queryByUserId(@Param("userId") Long userId);

    /**
     * 根据“用户ID”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据“用户ID”、“角色ID”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    UserRoleEntity queryByUserIdRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据“用户ID”、“角色ID”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByUserIdRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    List<Long> queryRoleIdsByUserId(@Param("userId") Long userId);

    List<UserRoleEntity> queryByUserIds(@Param("userIds") List<Long> userIds);

}
