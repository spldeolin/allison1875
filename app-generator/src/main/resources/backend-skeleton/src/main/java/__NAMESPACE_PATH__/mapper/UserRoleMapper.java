package __NAMESPACE__.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.entity.UserRoleEntity;

public interface UserRoleMapper {

    int batchInsert(@Param("entities") List<UserRoleEntity> entities);

    int deleteByUserId(@Param("userId") Long userId);

    List<Long> queryRoleIdsByUserId(@Param("userId") Long userId);

    List<UserRoleEntity> queryByUserIds(@Param("userIds") List<Long> userIds);

}
