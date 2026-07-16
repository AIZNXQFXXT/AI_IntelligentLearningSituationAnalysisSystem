package com.campus.backend.service;

import com.campus.common.vo.SuggestionVO;

public interface SuggestionService {
    SuggestionVO getSuggestion(Long studentId, String semester, Long callerId);
}
