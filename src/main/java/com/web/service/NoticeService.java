package com.web.service;

import com.web.repo.Notice;

import java.util.List;

public interface NoticeService {

    List<Notice> findAll();
    int updateviewsByseq(Long seq);

    void noticeRegister(Notice notice);

    void noticeDelete(Notice notice);
}
