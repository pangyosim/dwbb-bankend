package com.web.service;

import com.web.repo.QnA;

import java.util.List;

public interface QnAService {

    List<QnA> findAll();
    int updateviewsByseq(Long seq);
    void qnaRegister(QnA qna);

    void deleteqna(QnA qna);
}
