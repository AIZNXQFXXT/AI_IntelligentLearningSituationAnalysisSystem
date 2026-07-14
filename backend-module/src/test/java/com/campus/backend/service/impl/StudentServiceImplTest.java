package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.StudentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentMapper studentMapper;

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
}
