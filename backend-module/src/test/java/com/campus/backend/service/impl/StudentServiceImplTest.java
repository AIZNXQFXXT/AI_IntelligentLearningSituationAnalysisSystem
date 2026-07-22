package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Student;
import com.campus.backend.entity.Teacher;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.mapper.StudentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentMapper studentMapper;

    @Mock private com.campus.backend.mapper.ClassMapper classMapper;
    @Mock private com.campus.backend.mapper.TeacherMapper teacherMapper;
    @Mock private com.campus.backend.mapper.TeachingTaskMapper teachingTaskMapper;

    @Test
    void findByStudentNo_shouldReturnStudent_whenExists() {
        StudentServiceImpl service = new StudentServiceImpl(null, null, studentMapper, null, null, null, null);

        Student expected = new Student();
        expected.setId(1L);
        expected.setStudentNo("2024001");
        expected.setName("张三");

        when(studentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(expected);

        Student result = service.findByStudentNo("2024001");

        assertNotNull(result);
        assertEquals("2024001", result.getStudentNo());
        assertEquals("张三", result.getName());
    }

    @Test
    void findByStudentNo_shouldReturnNull_whenNotExists() {
        StudentServiceImpl service = new StudentServiceImpl(null, null, studentMapper, null, null, null, null);

        when(studentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Student result = service.findByStudentNo("9999999");

        assertNull(result);
    }

    @Test
    void pageList_teacherRole_shouldUnionTeachingTaskAndHeadTeacherClasses() {
        // 构造依赖: classMapper, teacherMapper, teachingTaskMapper 用 mock, 其余 null
        StudentServiceImpl service = new StudentServiceImpl(
                null, null, studentMapper, null, classMapper, teacherMapper, teachingTaskMapper);

        Teacher teacher = new Teacher();
        teacher.setId(5L);

        // 教学任务所带班级: classId=10
        TeachingTask task = new TeachingTask();
        task.setClassId(10L);

        // 班主任所带班级: id=20
        ClassInfo headClass = new ClassInfo();
        headClass.setId(20L);

        when(teacherMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(teacher);
        when(teachingTaskMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(java.util.List.of(task));
        when(classMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(java.util.List.of(headClass));

        Page<Student> page = new Page<>(1, 20);
        when(studentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(inv -> {
                    Page<Student> p = inv.getArgument(0);
                    p.setRecords(java.util.List.of());
                    p.setTotal(0);
                    return p;
                });

        IPage<Student> result = service.pageList(1, 20, null, null, "TEACHER", 1L);

        // 关键: 两条来源都应被查询 (证明并集逻辑生效)
        verify(teacherMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(teachingTaskMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(classMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        assertNotNull(result);
    }
}
