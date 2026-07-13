package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.ClassInfo;
import com.campus.common.dto.ClassDTO;
import java.util.List;

public interface ClassService {
    ClassInfo create(ClassDTO dto);
    ClassInfo update(ClassDTO dto);
    void delete(Long id);
    ClassInfo findById(Long id);
    IPage<ClassInfo> pageList(int page, int size, String keyword);
    List<ClassInfo> listAll();
}