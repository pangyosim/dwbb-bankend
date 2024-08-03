package com.web.controller;

import com.web.repo.Bank;
import com.web.repo.Park;
import com.web.service.BankService;
import com.web.service.ParkService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.lang.*;
import java.util.List;

@CrossOrigin("https://www.dwbb.online/")
@RestController
@PropertySource("classpath:application.properties")
public class MapController {

    @Value("${openapi.servicekey}")
    private String servicekey;
    private final BankService bs;
    private final ParkService ps;
    public MapController(BankService bs, ParkService ps) {this.bs = bs; this.ps= ps;}

    @PostMapping("/bank-data")
    @CrossOrigin
    public JSONArray bank_data_method(@RequestBody Bank bk) {
        try {
            // 은행 대기인원 API로 현재 대기인원 조회
            StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/B190021/totBrStateInq/gettotBrStateInq"); /*URL*/
            urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + servicekey); /*Service Key*/
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
//            System.out.println("Response code: " + conn.getResponseCode());
            BufferedReader rd;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();
            JSONParser parser = new JSONParser();
            // API 응답 데이터에 따라 catch 예외 던지기
            try{
                Object obz = parser.parse(sb.toString());
                JSONObject obj = (JSONObject)obz;
                JSONArray wait_arr = (JSONArray) obj.get("brcdList");
                // DB에 저장되어있는 은행정보 불러오기
                List<Bank> bank_data = bs.findAll();
                JSONArray tmp_arr = new JSONArray();
                // 현재 위치에서 5km이내 은행 조회
                // for-loop
                for(Bank obb : bank_data){
                    double distance = Math.round(Haversine_formula_method(bk.getGeox(), bk.getGeoy(), obb.getGeoy(), obb.getGeox()) * 100) / 100.0;
                    if( distance < 5.0 ) {
                        obb.setDistance(distance);
                        tmp_arr.add(obb);
                    }
                }
                JSONArray res = new JSONArray();
                if(wait_arr.size()>10) {
                    for (Object wait_obj : wait_arr) {
                        JSONObject wait_json_obj = (JSONObject) wait_obj;
                        for (Object tmp_obj : tmp_arr) {
                            Bank tmp_bank_obj = (Bank) tmp_obj;
                            if (tmp_bank_obj.getBrcd().equals(wait_json_obj.get("brcd"))) {
                                JSONObject tmp_json_obj = new JSONObject();
                                tmp_json_obj.put("brcd", tmp_bank_obj.getBrcd());
                                tmp_json_obj.put("krnbrm", tmp_bank_obj.getKrnbrm());
                                tmp_json_obj.put("brncnwbscadr", tmp_bank_obj.getBrncnwbscadr());
                                tmp_json_obj.put("brncTel", tmp_bank_obj.getBrncTel());
                                tmp_json_obj.put("rprsFax", tmp_bank_obj.getRprsFax());
                                tmp_json_obj.put("geox", tmp_bank_obj.getGeox());
                                tmp_json_obj.put("geoy", tmp_bank_obj.getGeoy());
                                tmp_json_obj.put("distance", tmp_bank_obj.getDistance());
                                tmp_json_obj.put("tlwnList", wait_json_obj.get("tlwnList"));
                                res.add(tmp_json_obj);
                            }
                        }
                    }
                } else {
                    res.addAll(tmp_arr);
                }
                return res;
            } catch (ParseException pe){
                JSONArray API_err = new JSONArray();
                List<Bank> bank_data = bs.findAll();
                JSONArray tmp_arr = new JSONArray();
                // 현재 위치에서 5km이내 은행 조회
                // for-loop
                for(Bank obb : bank_data){
                    double distance = Math.round(Haversine_formula_method(bk.getGeox(), bk.getGeoy(), obb.getGeoy(), obb.getGeox()) * 100) / 100.0;
                    if( distance < 5.0 ) {
                        obb.setDistance(distance);
                        tmp_arr.add(obb);
                    }
                }
                API_err.add("Bank API 점검");
                API_err.addAll(tmp_arr);
                return API_err;

            }
            // stream
//            bank_data.stream()
//                    .filter(obb -> {
//                        double distance = Math.round(Haversine_formula_method(bk.getGeox(), bk.getGeoy(), obb.getGeoy(), obb.getGeox()) * 100) / 100.0;
//                        if( distance < 7 ) {
//                            res.addAll(new ArrayList<>(){{
//                                add(obb.getBankseq().toString());
//                                add(obb.getBrcd());
//                                add(obb.getBrncnwbscadr());
//                                add(obb.getBrncTel());
//                                add(obb.getRprsFax());
//                                add(Double.toString(obb.getGeox()));
//                                add(Double.toString(obb.getGeoy()));
//                                add(Double.toString(distance));
//                            }});
//                        }
//                        return true;
//                    })
//                    .collect(Collectors.toList());
        } catch (Exception e){
            e.fillInStackTrace();
        }
        return null;
    }

    @PostMapping("/park-data")
    @CrossOrigin
    public JSONArray park_data_method(@RequestBody Park pk){
        List<Park> park_data =ps.getParkingList(new Park());
        JSONArray res = new JSONArray();
        for(Park pob : park_data){
            double distance = Math.round(Haversine_formula_method(pk.getLng(), pk.getLat(), pob.getLng(), pob.getLat()) * 100) / 100.0;
            if( distance < 5.0 ) {
                pob.setDistance(distance);
                res.add(pob);
            }
        }
        return res;
    }

    // Haversine formula
    private Double Haversine_formula_method(double lat1, double lon1, double lat2, double lon2) {
            double R = 6371;
            double dLat = deg2rad(lat2 - lat1);
            double dLon = deg2rad(lon2 - lon1);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
    private Double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
    // Haversine formula end

}
