package com.web.service;

import com.web.persistence.BankRepository;
import com.web.repo.Bank;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankServiceImpl implements BankService {

    private final BankRepository brs;

    @Autowired
    public BankServiceImpl(BankRepository brs) {
        this.brs = brs;
    }

    @Override
    public void preDataSave(Bank bank) {brs.save(bank);}

    @Override
    public List<Bank> findAll() {return (List<Bank>) brs.findAll();}
}
