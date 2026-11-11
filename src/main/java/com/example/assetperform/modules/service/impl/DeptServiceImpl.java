package com.example.assetperform.modules.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.assetperform.common.exception.BusinessException;
import com.example.assetperform.modules.domain.dto.DeptCreateDTO;
import com.example.assetperform.modules.domain.dto.DeptQueryDTO;
import com.example.assetperform.modules.domain.dto.DeptUpdateDTO;
import com.example.assetperform.modules.domain.entity.DeptEntity;
import com.example.assetperform.modules.domain.entity.UserEntity;
import com.example.assetperform.modules.domain.vo.DeptTreeVO;
import com.example.assetperform.modules.mapper.DeptMapper;
import com.example.assetperform.modules.mapper.UserMapper;
import com.example.assetperform.modules.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, DeptEntity> implements DeptService {

    private final UserMapper userMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    // Redis的部门TreeKEY前缀
    public static final String DEPT_TREE_KEY = "sys:dept:tree";
    
    // 新增部门
    @Override 
    public void createDept(DeptCreateDTO dto) {
        // 1. 同一个父部门下，不能有相同的部门名称
        boolean exist = this.lambdaQuery()
                .eq(DeptEntity::getParentId, dto.getParentId())
                .eq(DeptEntity::getName, dto.getName())
                .exists();
        if (exist) {
            throw new BusinessException("创建失败，请检查是否重名");
        }

        // 2. DTO 转 Entity 入库
        DeptEntity deptEntity = new DeptEntity();
        BeanUtils.copyProperties(dto, deptEntity);
        this.save(deptEntity);

        // 缓存淘汰
        stringRedisTemplate.delete(DEPT_TREE_KEY);
    }

    // 修改部门
    @Override
    public void updateDept(DeptUpdateDTO dto) {
        Long id = dto.getId();

        // 1. 防护校验：上级部门不能是自己
        if (id.equals(dto.getParentId())) {
            throw new BusinessException("上级部门不能是自己！");
        }

        // 2. 检查部门是否存在
        DeptEntity oldDept = this.getById(id);
        if (oldDept == null) {
            throw new BusinessException("要修改的部门不存在！");
        }

        // 3. 校验同级部门名称是否重复（排除自己本身）
        boolean exist = this.lambdaQuery()
                .eq(DeptEntity::getParentId, dto.getParentId())
                .eq(DeptEntity::getName, dto.getName())
                .ne(DeptEntity::getId, id)
                .exists();
        if (exist) {
            throw new BusinessException("该部门名称已存在！");
        }

        // 4. DTO 转 Entity 并更新
        DeptEntity deptEntity = new DeptEntity();
        BeanUtils.copyProperties(dto, deptEntity);
        this.updateById(deptEntity);

        // 缓存淘汰
        stringRedisTemplate.delete(DEPT_TREE_KEY);
    }

    // 列出所有的 不加分页
    @Override
    public List<DeptEntity> listAll() {
        return this.lambdaQuery()
                .orderByAsc(DeptEntity::getSort)
                .list();
    }

    // 分页查询所有部门
    @Override
    public Page<DeptEntity> listPage(DeptQueryDTO deptQueryDTO) {
        Page<DeptEntity> page = new Page<>(
                deptQueryDTO.getPageNum(),
                deptQueryDTO.getPageSize()
        );

        return this.lambdaQuery()
                .orderByAsc(DeptEntity::getSort)
                .page(page);
    }

    // 获取部门树
    @Override 
    public List<DeptTreeVO> getDeptTree() {
        // 先读Redis
        try{
            String deptJson = stringRedisTemplate.opsForValue().get(DEPT_TREE_KEY);

            if(StringUtils.hasText(deptJson)) {
                return objectMapper.readValue(deptJson, new TypeReference<List<DeptTreeVO>>() {});
            }
        }catch (Exception e){
            // Redis偶发异常，自动降级去数据库查
        }

        // 未命中 继续。
        List<DeptEntity> deptList = this.lambdaQuery()
                .eq(DeptEntity::getStatus, 0)
                .orderByAsc(DeptEntity::getSort)
                .list();

        // 将 Entity 转换为 VO
        List<DeptTreeVO> voList = deptList.stream().map(dept -> {
            DeptTreeVO deptTreeVO = new DeptTreeVO();
            BeanUtils.copyProperties(dept, deptTreeVO);
            return deptTreeVO;
        }).toList();

        // 3. 按 parentId 对子部门进行分组
        Map<Long, List<DeptTreeVO>> childrenMap = voList.stream()
                .collect(Collectors.groupingBy(DeptTreeVO::getParentId));

        // 4. 为每个节点挂载其子节点
        voList.forEach(node ->
                node.setChildren(childrenMap.getOrDefault(node.getId(), new ArrayList<>()))
        );

        // 5. 过滤出顶级根部门（parentId 为 0）返回
        List<DeptTreeVO> list = voList.stream()
                .filter(node -> node.getParentId() == 0L)
                .toList();

        // 写入redis
        try{
            stringRedisTemplate.opsForValue().set(DEPT_TREE_KEY,
                    objectMapper.writeValueAsString(list),
                    Duration.ofHours(24)
                    );
        } catch (Exception e){
            // 写入失败不作处理，下一次自动降级
        }

        return list;
    }

    // 删除部门
    @Override 
    public void deleteDept(Long id) {
        DeptEntity dept = this.getById(id);
        if (dept == null) {
            throw new BusinessException("该部门不存在！");
        }

        if (dept.getParentId() != null && dept.getParentId() == 0L) {
            throw new BusinessException("顶级公司/组织根节点受系统级保护，禁止删除！");
        }

        // 2. 如果部门下面有子部门则无法删除
        boolean hasChildren = this.lambdaQuery()
                .eq(DeptEntity::getParentId, id)
                .exists();
        if (hasChildren) {
            throw new BusinessException("该部门下存在子部门，无法删除！");
        }

        // 3. 如果部门下仍有在职员工，则禁止删除
        Long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getDeptId, id)
        );
        if (userCount != null && userCount > 0) {
            throw new BusinessException("该部门下仍有分配的员工，无法直接删除！请先将员工调整到其他部门。");
        }

        // 自动逻辑删除
        this.removeById(id);

        // 缓存淘汰
        stringRedisTemplate.delete(DEPT_TREE_KEY);
    }
}
