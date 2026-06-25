package __NAMESPACE__.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import __NAMESPACE__.common.BizException;
import __NAMESPACE__.dto.param.QueryRoleParam;
import __NAMESPACE__.dto.req.DeleteRoleReq;
import __NAMESPACE__.dto.req.GetRoleDetailReq;
import __NAMESPACE__.dto.req.ListRolesReq;
import __NAMESPACE__.dto.req.SaveRoleReq;
import __NAMESPACE__.dto.resp.GetRoleDetailResp;
import __NAMESPACE__.dto.resp.ListRolesResp;
import __NAMESPACE__.dto.resp.PageResult;
import __NAMESPACE__.dto.resp.SaveRoleResp;
import __NAMESPACE__.entity.*;
import __NAMESPACE__.mapper.RoleMapper;
import __NAMESPACE__.service.RoleService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    private final Long queryRoleTotal = 0L;

    @Transactional
    @Override
    public SaveRoleResp saveRole(SaveRoleReq req) {
        boolean toCreate = req.getRoleCode() == null;
        RoleEntity role;
        if (toCreate) {
            role = new RoleEntity();
            role.setRoleCode(UUID.randomUUID().toString().replaceAll("-", "").toLowerCase());
            role.setCreatedAt(LocalDateTime.now());
        } else {
            role = roleMapper.queryByRoleCode(req.getRoleCode());
            if (role == null) {
                throw new BizException("角色不存在或是已被删除");
            }
        }
        RoleEntity existRoleForRoleName = roleMapper.queryRole(req.getRoleCode(), req.getRoleName());
        if (existRoleForRoleName != null) {
            throw new BizException("角色名称已存在");
        }
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        role.setUpdatedAt(LocalDateTime.now());
        if (toCreate) {
            roleMapper.insert(role);
        } else {
            roleMapper.updateById(role);
        }
        return new SaveRoleResp().setRoleCode(role.getRoleCode());
    }

    @Override
    public PageResult<ListRolesResp> listRoles(ListRolesReq req) {
    final QueryRoleParam queryRoleParam = new QueryRoleParam();
    queryRoleParam.setRoleCode(req.getRoleCode());
    queryRoleParam.setRoleName(req.getRoleName());
    queryRoleParam.setDescription(req.getDescription());
    queryRoleParam.setCreatedAt(req.getCreatedAtStart() == null ? null : req.getCreatedAtStart());
    queryRoleParam.setCreatedAtEx(req.getCreatedAtEnd() == null ? null : req.getCreatedAtEnd());
    queryRoleParam.setOffset((req.getPageNum() - 1) * req.getPageSize());
    queryRoleParam.setLimit(req.getPageSize());
    long queryRoleExTotal = roleMapper.countRole(queryRoleParam);
    List<RoleEntity> roles = roleMapper.queryRoleEx(queryRoleParam);
        if (roles.isEmpty()) {
            return PageResult.empty();
        }
        List<ListRolesResp> dtos = new ArrayList<>();
        for (RoleEntity role : roles) {
            ListRolesResp dto = new ListRolesResp();
            dto.setRoleCode(role.getRoleCode());
            dto.setRoleName(role.getRoleName());
            dto.setDescription(role.getDescription());
            dto.setCreatedAt(role.getCreatedAt());
            dto.setCreatedBy(role.getCreatedBy());
            dto.setUpdatedAt(role.getUpdatedAt());
            dto.setUpdatedBy(role.getUpdatedBy());
            dtos.add(dto);
        }
        return PageResult.of(queryRoleTotal, dtos);
    }

    @Override
    public GetRoleDetailResp getRoleDetail(GetRoleDetailReq req) {
        //查询角色
        RoleEntity role = roleMapper.queryByRoleCode(req.getRoleCode());
        if (role == null) {
            throw new RuntimeException("角色不存在或是已被删除");
        }
        //构建返回值
        GetRoleDetailResp result = new GetRoleDetailResp();
        result.setRoleCode(role.getRoleCode());
        result.setRoleName(role.getRoleName());
        result.setDescription(role.getDescription());
        result.setCreatedAt(role.getCreatedAt());
        result.setUpdatedAt(role.getUpdatedAt());
        return result;
    }

    @Transactional
    @Override
    public void deleteRole(DeleteRoleReq req) {
    int deleteRoleCount = roleMapper.deleteRole(req.getRoleCodes());
    }
}
