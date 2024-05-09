package com.web.repo;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="bankdata")
@SequenceGenerator(
        name = "BANK_SEQ_GENERATOR",
        sequenceName = "BANK_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Bank {

    public Bank () {}

    public Bank(Long bankseq, String brcd, String krnbrm, String brncnwbscadr, String brncTel, String rprsFax, double geox, double geoy) {
        this.bankseq = bankseq;
        this.brcd = brcd;
        this.krnbrm = krnbrm;
        this.brncnwbscadr = brncnwbscadr;
        this.brncTel = brncTel;
        this.rprsFax = rprsFax;
        this.geox = geox;
        this.geoy = geoy;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BANK_SEQ_GENERATOR")
    private Long bankseq;
    @Column(name= "brcd", nullable = false)
    private String brcd;
    @Column(name= "krnbrm", nullable = false)
    private String krnbrm;
    @Column(name= "brncnwbscadr", nullable = false)
    private String brncnwbscadr;
    @Column(name="brncTel", nullable = false)
    private String brncTel;
    @Column(name="rprsFax", nullable = false)
    private String rprsFax;
    @Column(name="geox", nullable = false, length = 255)
    private double geox;
    @Column(name="geoy", nullable = false, length = 255)
    private double geoy;
    @Column(name="distance", length = 255)
    private double distance;
}
