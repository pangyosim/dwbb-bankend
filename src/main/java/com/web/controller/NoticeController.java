package com.web.controller;

import com.web.repo.Notice;
import com.web.repo.QnA;
import com.web.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
@CrossOrigin("https://www.dwbb.online/")
@RestController
public class NoticeController {
    private final NoticeService ns;

    @Autowired
    public NoticeController(NoticeService ns) {
        this.ns = ns;
    }

    @PostMapping("/notice-all")
    @CrossOrigin
    public List<Notice> noticeall_method (){
        try{
            return ns.findAll();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return null;
    }

    @PostMapping("/notice-views")
    @CrossOrigin
    public int update_views_method(@RequestBody Notice notice){
        try{
            return ns.updateviewsByseq(notice.getNoticeseq());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return -1;
    }

    @PostMapping("/notice-register")
    @CrossOrigin
    public String register_notice_method(@RequestBody Notice notice){
        try {
            notice.setNoticecreateday(new Date());
            notice.setNoticefile("C:\\TESTDATAVOLUME");
            ns.noticeRegister(notice);
            return "register-success";
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return "";
    }

    @PostMapping("/notice-delete")
    @CrossOrigin
    public String delete_notice_method(@RequestBody Notice notice){
        ns.noticeDelete(notice);
        return "delete-success";
    }
}
