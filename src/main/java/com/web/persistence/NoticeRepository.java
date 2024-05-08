package com.web.persistence;

import com.web.repo.Notice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends CrudRepository<Notice,Long> {
    List<Notice> findAll();

    @Query(value = "UPDATE dwbbnotice SET NOTICEVIEWS=NOTICEVIEWS+1 WHERE NOTICESEQ=:seq", nativeQuery = true)
    int updateviewsByseq(@Param("seq") Long seq);
}
