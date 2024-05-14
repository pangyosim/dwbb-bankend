package com.web.service;

import com.web.repo.QnA;

import java.util.List;

public interface QnAService {

    List<QnA> findAll();
    int updateviewsByseq(Long seq);
    int updatecommentsByseq(Long seq, String comments);
    void qnaRegister(QnA qna);

    void deleteqna(QnA qna);
}
