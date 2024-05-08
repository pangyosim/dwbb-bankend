package com.web.persistence;

import com.web.repo.Park;
import org.springframework.data.repository.CrudRepository;

public interface ParkRepository extends CrudRepository<Park,Long> {

}
