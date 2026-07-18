package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.converter.CourseConverter;
import com.campus.backend.entity.Course;
import com.campus.backend.mapper.CourseMapper;
import com.campus.backend.service.CourseService;
import com.campus.common.dto.CourseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseConverter courseConverter;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public Course create(CourseDTO dto) {
        Course existing = courseMapper.selectByNameIncludeDeleted(dto.getName());
        if (existing != null) {
            existing.setIsDeleted(0);
            existing.setUpdatedAt(LocalDateTime.now());
            courseMapper.recoverByName(dto.getName());
            return existing;
        }
        Course entity = courseConverter.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        courseMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public Course update(CourseDTO dto) {
        Course entity = courseConverter.toEntity(dto);
        entity.setUpdatedAt(LocalDateTime.now());
        courseMapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        courseMapper.deleteById(id);
    }

    @Override
    public Course findById(Long id) {
        return courseMapper.selectById(id);
    }

    @Override
    public IPage<Course> pageList(int page, int size, String keyword) {
        Page<Course> p = new Page<>(page, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getName, keyword)
                    .or().like(Course::getType, keyword);
        }
        wrapper.orderByAsc(Course::getId);
        return courseMapper.selectPage(p, wrapper);
    }

    @Override
    public void batchImport(MultipartFile file) {

    }

    @Override
    @Transactional
    public void toggleStatus(Long id, Integer status) {
        Course entity = courseMapper.selectById(id);
        entity.setStatus(status);
        courseMapper.updateById(entity);
    }

    @Override
    public List<Course> listAll() {
        return courseMapper.selectList(null);
    }
}
