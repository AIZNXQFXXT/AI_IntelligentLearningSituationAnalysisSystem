package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.converter.ExamConverter;
import com.campus.backend.entity.Exam;
import com.campus.backend.mapper.ExamMapper;
import com.campus.backend.service.ExamService;
import com.campus.common.dto.ExamDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ExamServiceImpl implements ExamService {
    private final ExamMapper examMapper;
    private final ExamConverter examConverter;

    @Override
    @Transactional
    public Exam create(ExamDTO dto) {
        Exam entity = examConverter.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        examMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public Exam update(ExamDTO dto) {
        Exam entity = examConverter.toEntity(dto);
        entity.setUpdatedAt(LocalDateTime.now());
        examMapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        examMapper.deleteById(id);
    }

    @Override
    public Exam findById(Long id) {
        return examMapper.selectById(id);
    }

    @Override
    public IPage<Exam> pageList(int page, int size, String keyword) {
        Page<Exam> p = new Page<>(page, size);
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Exam::getName, keyword)
                    .or().like(Exam::getType, keyword)
                    .or().like(Exam::getSemester, keyword);
        }
        wrapper.orderByAsc(Exam::getId);
        return examMapper.selectPage(p, wrapper);
    }

    @Override
    public void batchImport(MultipartFile file) {

    }

    @Override
    @Transactional
    public void toggleArchive(Long id) {
        Exam entity = examMapper.selectById(id);
        Integer archive = entity.getIsArchived();
        if (archive == 1) {
            entity.setIsArchived(0);
        } else {
            entity.setIsArchived(1);
        }
        examMapper.updateById(entity);
    }
}
