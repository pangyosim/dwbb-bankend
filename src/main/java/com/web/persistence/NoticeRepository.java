package com.web.persistence;

import com.web.repo.Notice;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Transactional
public interface NoticeRepository extends CrudRepository<Notice,Long> {
    List<Notice> findAll();
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE dwbbnotice SET NOTICEVIEWS=NOTICEVIEWS+1 WHERE NOTICESEQ=:seq", nativeQuery = true)
    int updateviewsByseq(@Param("seq") Long seq);
}
