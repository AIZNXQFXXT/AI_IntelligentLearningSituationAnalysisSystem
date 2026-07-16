package com.campus.backend.service;

import com.campus.common.dto.AICommentDTO;
import com.campus.common.vo.CommentVO;
import com.campus.common.vo.PageResult;
import java.util.List;

public interface CommentService {
    CommentVO generateSingle(AICommentDTO dto, Long teacherId);
    CommentVO update(Long id, String content, Long teacherId);
    PageResult<CommentVO> pageList(int page, int size, Long classId, String semester);
    List<CommentVO> exportList(Long classId, String semester);
    PageResult<CommentVO> listByStudent(int page, int size, Long studentId, String semester);
}
