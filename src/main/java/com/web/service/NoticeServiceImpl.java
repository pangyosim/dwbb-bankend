package com.web.service;

import com.web.persistence.NoticeRepository;
import com.web.repo.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository nrs;

    @Autowired
    public NoticeServiceImpl (NoticeRepository nrs) {
        this.nrs = nrs;
    }

    @Override
    public List<Notice> findAll() {
        return nrs.findAll();
    }

    @Override
    public int updateviewsByseq(Long seq) {
        return nrs.updateviewsByseq(seq);
    }

    @Override
    public void noticeRegister(Notice notice) { nrs.save(notice);}

    @Override
    public void noticeDelete(Notice notice) { nrs.delete(notice);}
}
