package com.web.repo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "dwbbqna")
@SequenceGenerator(
        name = "DWBBQNA_SEQ_GENERATOR",
        sequenceName = "DWBBQNA_SEQ",
        initialValue = 1,
        allocationSize = 1)
@Cacheable(value = false)
public class QnA {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DWBBQNA_SEQ_GENERATOR")
    private Long qnaseq;

    @Column(name = "qnatitle", nullable = false)
    private String qnatitle;

    @Column(name = "qnacontents", nullable = false)
    private String qnacontents;

    @Column(name = "qnaviews", nullable = false)
    private int qnaviews;

    @Column(name = "qnastate", nullable = false)
    private boolean qnastate;

    @Column(name = "qnacreateday", nullable = false)
    private Date qnacreateday;

    @Column(name = "qnanickname", nullable = false)
    private String qnanickname;

    @Column(name = "comments", nullable = true)
    private String comments;
}
