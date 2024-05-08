package com.web.service;

import com.web.persistence.QnARepository;
import com.web.repo.QnA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QnAServiceImpl implements QnAService{

    private final QnARepository qar;
    @Autowired
    public QnAServiceImpl(QnARepository qar) {this.qar = qar;}

    @Override
    public List<QnA> findAll() {
        return qar.findAll();
    }

    @Override
    public int updateviewsByseq(Long seq) {
        return qar.updateviewsByseq(seq);
    }

    @Override
    public void qnaRegister(QnA qna) {qar.save(qna);}

    @Override
    public void deleteqna(QnA qna) { qar.delete(qna);}
}
