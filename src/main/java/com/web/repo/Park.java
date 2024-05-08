package com.web.repo;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="parkdata")
@SequenceGenerator(
        name = "PARK_SEQ_GENERATOR",
        sequenceName = "PARK_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Park {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PARK_SEQ_GENERATOR")
    private Long parkseq;

    @Column(name="type")
    private String type;

    // 주차장 위치정보
    @Column(name = "lat")
    private double lat;
    @Column(name="lng")
    private double lng;

    // 주차장 이름
    @Column(name = "pkname")
    private String pkname;
    // 운영구분명
    @Column(name="pkrule")
    private String pkrule;
    // 총 주채대수
    @Column(name="capacity")
    private int capacity;

    // 유/무료 정보
    @Column(name = "paytype")
    private String paytype;

    // 운영시간 및 유/무료 정보
    @Column(name = "holidaytime")
    private String holidaytime;
    @Column(name = "holidaypaytype")
    private String holidaypaytype;

    @Column(name = "weekdaytime")
    private String weekdaytime;

    @Column(name = "weekendtime")
    private String weekendtime;
    @Column(name = "saturdaypay")
    private String saturdaypay;

    // 요금
    @Column(name = "rates")
    private String rates;
    @Column(name = "timerates")
    private String timerates;
    @Column(name = "addrates")
    private String addrates;
    @Column(name = "addtimerates")
    private String addtimerates;
    @Column(name = "daymaximum")
    private String daymaximum;
    @Column(name = "fullmonthly")
    private int fullmonthly;

    @Column(name = "pkaddr")
    private String pkaddr;
    @Column(name = "pkcode")
    private String pkcode;

    @Column(name = "tel")
    private String tel;

    @Column(name = "nightyn")
    private String nightyn;

    @Column(name="distance", length = 255)
    private double distance;
}
