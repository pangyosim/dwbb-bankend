package com.web.service;

import com.web.persistence.ParkRepository;
import com.web.repo.Park;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkServiceImpl implements ParkService{

    private final ParkRepository ps;
    @Autowired
    public ParkServiceImpl(ParkRepository ps) {
        this.ps = ps;
    }

    @Override
    public void insertParking(Park pe) {ps.save(pe);}

    @Override
    public List<Park> getParkingList(Park pe) {
        return (List<Park>) ps.findAll();
    }
}
