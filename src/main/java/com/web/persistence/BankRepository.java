package com.web.persistence;

import com.web.repo.Bank;
import org.springframework.data.repository.CrudRepository;

public interface BankRepository extends CrudRepository<Bank,Long> {

}
