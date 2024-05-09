package com.web.service;

import com.web.repo.Bank;

import java.util.List;


public interface BankService {

    void preDataSave(Bank bank);

    List<Bank> findAll();
}
