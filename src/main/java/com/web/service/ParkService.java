package com.web.service;

import com.web.repo.Park;

import java.util.List;

public interface ParkService {

    List<Park> getParkingList(Park pe);

    void insertParking(Park pe);
}
