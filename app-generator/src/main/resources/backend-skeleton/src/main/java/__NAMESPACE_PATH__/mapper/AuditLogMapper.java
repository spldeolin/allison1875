package __NAMESPACE__.mapper;

import __NAMESPACE__.entity.AuditLogEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.MapKey;
import __NAMESPACE__.dto.param.ListAuditLogsParam;

/**
 * 审计日志
 * <p>audit_log
 *
 * @author Deolin 2026-06-27
 */
public interface AuditLogMapper {

    /**
     * 插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int insert(AuditLogEntity entity);

    /**
     * 批量插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsert(@Param("entities") List<AuditLogEntity> entities);

    /**
     * 批量插入，为null的属性会被作为null插入
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int batchInsertEvenNull(@Param("entities") List<AuditLogEntity> entities);

    /**
     * 根据ID更新数据，忽略值为null的属性
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateById(AuditLogEntity entity);

    /**
     * 根据ID更新数据，为null属性对应的字段会被更新为null
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int updateByIdEvenNull(AuditLogEntity entity);

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
    AuditLogEntity queryById(Long id);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<AuditLogEntity> queryByIds(@Param("ids") List<Long> ids);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("id")
    Map<Long, AuditLogEntity> queryByIdsEachId(@Param("ids") List<Long> ids);

    /**
     * 根据“业务主键”查询数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    AuditLogEntity queryByAuditLogCode(@Param("auditLogCode") String auditLogCode);

    /**
     * 根据“业务主键”删除数据
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    int deleteByAuditLogCode(@Param("auditLogCode") String auditLogCode);

    /**
     * 根据多个ID查询
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    List<AuditLogEntity> queryByAuditLogCodes(@Param("auditLogCodes") List<String> auditLogCodes);

    /**
     * 根据多个ID查询，并以ID作为key映射到Map
     * <p>
     * <p>Any modifications may be overwritten by future code generations.
     */
    @MapKey("auditLogCode")
    Map<String, AuditLogEntity> queryByAuditLogCodesEachAuditLogCode(@Param("auditLogCodes") List<String> auditLogCodes);

    long countListAuditLogs(ListAuditLogsParam listAuditLogsParam);

    List<AuditLogEntity> listAuditLogs(ListAuditLogsParam listAuditLogsParam);

}
