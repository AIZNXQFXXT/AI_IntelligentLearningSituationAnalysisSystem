package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Teacher;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.TeacherMapper;
import com.campus.backend.mapper.TeachingTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassServiceImplTest {

    @Mock private ClassMapper classMapper;
    @Mock private TeacherMapper teacherMapper;
    @Mock private TeachingTaskMapper teachingTaskMapper;

    @Test
    void findMyClasses_shouldUnionTeachingTaskAndHeadTeacherClasses() {
        // 构造: classMapper, teacherMapper, teachingTaskMapper 用 mock, 其余 (classConverter, studentMapper) null
        ClassServiceImpl service = new ClassServiceImpl(classMapper, null, null, teacherMapper, teachingTaskMapper);

        Teacher teacher = new Teacher();
        teacher.setId(5L);

        // 教学任务所带班级: classId=10
        TeachingTask task = new TeachingTask();
        task.setClassId(10L);

        // 班主任所带班级: id=20
        ClassInfo headClass = new ClassInfo();
        headClass.setId(20L);

        when(teacherMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(teacher);
        when(teachingTaskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(task));
        when(classMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(headClass));
        when(classMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(inv -> {
                    Page<ClassInfo> p = inv.getArgument(0);
                    p.setRecords(List.of());
                    p.setTotal(0);
                    return p;
                });

        var result = service.findMyClasses(1, 20, null, 1L);

        // 关键: 两条来源都应被查询
        verify(teacherMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(teachingTaskMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(classMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        assertNotNull(result);
    }
}
