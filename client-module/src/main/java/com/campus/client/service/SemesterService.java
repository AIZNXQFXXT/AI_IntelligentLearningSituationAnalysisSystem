package com.aicampus.service;

import com.aicampus.model.Exam;
import com.aicampus.model.PageResult;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SemesterService {
    public static List<String> getAllSemesters() throws Exception {
        PageResult<Exam> result = ExamService.getPage(1, 1000, null);
        return result.getRecords().stream()
                .map(Exam::getSemester)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }
}
