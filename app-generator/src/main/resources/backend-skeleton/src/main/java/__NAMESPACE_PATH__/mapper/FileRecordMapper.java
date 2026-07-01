package __NAMESPACE__.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import __NAMESPACE__.entity.FileRecordEntity;

/**
 * 文件记录
 * <p>file_record
 *
 * @author Deolin 2026-06-30
 */
public interface FileRecordMapper {

    /**
     * 插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int insert(FileRecordEntity entity);

    /**
     * 批量插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsert(@Param("entities") List<FileRecordEntity> entities);

    /**
     * 批量插入，为null的属性会被作为null插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsertEvenNull(@Param("entities") List<FileRecordEntity> entities);

    /**
     * 根据ID更新数据，忽略值为null的属性
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateById(FileRecordEntity entity);

    /**
     * 根据ID更新数据，为null属性对应的字段会被更新为null
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateByIdEvenNull(FileRecordEntity entity);

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
    FileRecordEntity queryById(Long id);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<FileRecordEntity> queryByIds(@Param("ids") List<Long> ids);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("id")
    Map<Long, FileRecordEntity> queryByIdsEachId(@Param("ids") List<Long> ids);

    /**
     * 根据“文件Key（uuid+扩展名），业务唯一键，业务表引用此值”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    FileRecordEntity queryByFileKey(@Param("fileKey") String fileKey);

    /**
     * 根据“文件Key（uuid+扩展名），业务唯一键，业务表引用此值”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByFileKey(@Param("fileKey") String fileKey);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<FileRecordEntity> queryByFileKeys(@Param("fileKeys") List<String> fileKeys);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("fileKey")
    Map<String, FileRecordEntity> queryByFileKeysEachFileKey(@Param("fileKeys") List<String> fileKeys);
}
