package com.web.repo;

import java.util.Date;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "dwbbnotice")
@SequenceGenerator(
        name = "NOTICE_SEQ_GENERATOR",
        sequenceName = "DWBBNOTICE_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTICE_SEQ_GENERATOR")
    private Long noticeseq;

    @Column(name = "noticeid", nullable = false)
    private String noticeid;
    @Column(name = "noticetitle", nullable = false)
    private String noticetitle;
    @Column(name = "noticecontents", nullable = false)
    private String noticecontents;
    @Column(name = "noticeviews", nullable = false)
    private int noticeviews;
    @Column(name = "noticefile", nullable = false)
    private String noticefile;
    @Column(name = "noticecreateday", nullable = false)
    private Date noticecreateday;

}
