package com.web.persistence;

import com.web.repo.QnA;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface QnARepository extends CrudRepository<QnA,Long> {
    List<QnA> findAll();
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE dwbbqna SET QNAVIEWS=QNAVIEWS+1 WHERE QNASEQ=:seq", nativeQuery = true)
    int updateviewsByseq(@Param("seq") Long seq);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE dwbbqna SET QNASTATE=1,COMMENTS=:comments WHERE QNASEQ=:seq", nativeQuery = true)
    int updatecommentsByseq(@Param("seq") Long seq,@Param("comments") String comments);
}