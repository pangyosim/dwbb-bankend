package com.web.controller;

import com.web.repo.Notice;
import com.web.repo.QnA;
import com.web.service.QnAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
@CrossOrigin("https://www.dwbb.online/")
@RestController
public class QnAController {

    private final QnAService qas;

    @Autowired
    public QnAController(QnAService qas) {
        this.qas = qas;
    }

    @PostMapping("/qna-all")
    @CrossOrigin
    public List<QnA> qnaall_method (){
        try {
            return qas.findAll();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return null;
    }

    @PostMapping("/qna-views")
    @CrossOrigin
    public int update_views_method(@RequestBody QnA qna){
        try{
            return qas.updateviewsByseq(qna.getQnaseq());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return -1;
    }

    @PostMapping("/qna-register")
    @CrossOrigin
    public String register_qna_method(@RequestBody QnA qna){
        qna.setQnacreateday(new Date());
        qas.qnaRegister(qna);
        return "register-success";
    }

    @PostMapping("/qna-delete")
    @CrossOrigin
    public String delete_qna_method(@RequestBody QnA qna){
        qas.deleteqna(qna);
        return "delete-success";
    }

    @PostMapping("/qna-comments")
    @CrossOrigin
    public String comments_qna_method(@RequestBody QnA qna){
        System.out.println(qna);
        qas.updatecommentsByseq(qna.getQnaseq(), qna.getComments());
        return "comments-success";
    }
}
