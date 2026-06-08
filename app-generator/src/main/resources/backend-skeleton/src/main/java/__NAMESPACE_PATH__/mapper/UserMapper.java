package __NAMESPACE__.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.dto.param.QueryUserParam;
import __NAMESPACE__.entity.UserEntity;

/**
 * 用户
 * <p>user
 *
 * @author Deolin 2026-06-07
 */
public interface UserMapper {

    /**
     * 插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int insert(UserEntity entity);

    /**
     * 批量插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsert(@Param("entities") List<UserEntity> entities);

    /**
     * 批量插入，为null的属性会被作为null插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsertEvenNull(@Param("entities") List<UserEntity> entities);

    /**
     * 根据ID更新数据，忽略值为null的属性
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateById(UserEntity entity);

    /**
     * 根据ID更新数据，为null属性对应的字段会被更新为null
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateByIdEvenNull(UserEntity entity);

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
    UserEntity queryById(Long id);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<UserEntity> queryByIds(@Param("ids") List<Long> ids);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("id")
    Map<Long, UserEntity> queryByIdsEachId(@Param("ids") List<Long> ids);

    /**
     * 根据“业务主键”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    UserEntity queryByUserCode(@Param("userCode") String userCode);

    /**
     * 根据“业务主键”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByUserCode(@Param("userCode") String userCode);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<UserEntity> queryByUserCodes(@Param("userCodes") List<String> userCodes);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("userCode")
    Map<String, UserEntity> queryByUserCodesEachUserCode(@Param("userCodes") List<String> userCodes);

    /**
     * 根据“用户名”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    UserEntity queryByUsername(@Param("username") String username);

    /**
     * 根据“用户名”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByUsername(@Param("username") String username);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<UserEntity> queryByUsernames(@Param("usernames") List<String> usernames);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("username")
    Map<String, UserEntity> queryByUsernamesEachUsername(@Param("usernames") List<String> usernames);

    /**
     * 根据“当前登录token”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    UserEntity queryByCurrentToken(@Param("currentToken") String currentToken);

    /**
     * 根据“当前登录token”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByCurrentToken(@Param("currentToken") String currentToken);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<UserEntity> queryByCurrentTokens(@Param("currentTokens") List<String> currentTokens);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("currentToken")
    Map<String, UserEntity> queryByCurrentTokensEachCurrentToken(@Param("currentTokens") List<String> currentTokens);

    UserEntity queryUser(@Param("userCode") String userCode, @Param("username") String username);

    long countUser(QueryUserParam queryUserParam);

    List<UserEntity> queryUserEx(QueryUserParam queryUserParam);

    int deleteUser(@Param("userCode") List<String> userCode);

}
