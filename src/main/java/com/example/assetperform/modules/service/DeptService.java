package com.example.assetperform.modules.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.assetperform.modules.domain.dto.DeptCreateDTO;
import com.example.assetperform.modules.domain.dto.DeptQueryDTO;
import com.example.assetperform.modules.domain.dto.DeptUpdateDTO;
import com.example.assetperform.modules.domain.entity.DeptEntity;
import com.example.assetperform.modules.domain.vo.DeptTreeVO;

import java.util.List;

public interface DeptService extends IService<DeptEntity> {
    
    // 新增部门
    void createDept(DeptCreateDTO dto);

    // 修改部门
    void updateDept(DeptUpdateDTO dto);

    // 获取所有部门列表
    List<DeptEntity> listAll();

    // 分页查询所有的部门信息
    Page<DeptEntity> listPage(DeptQueryDTO deptQueryDTO);

    // 获取部门树状列表
    List<DeptTreeVO> getDeptTree();

    // 删除部门
    void deleteDept(Long id);
}
