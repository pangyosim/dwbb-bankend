package com.web.persistence;

import com.web.repo.QnA;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QnARepository extends CrudRepository<QnA,Long> {
    List<QnA> findAll();
    @Query(value = "UPDATE dwbbqna SET QNAVIEWS=QNAVIEWS+1 WHERE QNASEQ=:seq", nativeQuery = true)
    int updateviewsByseq(@Param("seq") Long seq);
}
