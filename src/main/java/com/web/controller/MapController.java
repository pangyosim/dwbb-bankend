package com.web.controller;

import com.web.repo.Bank;
import com.web.repo.Park;
import com.web.service.BankService;
import com.web.service.ParkService;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParseException;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.lang.*;
import java.util.List;

@CrossOrigin("https://www.dwbb.online/")
@RestController
@PropertySource("classpath:application.properties")
public class MapController {

    @Value("${openapi.servicekey}")
    private String servicekey;

    @Value("${openapi.clientid}")
    private String clientId;

    @Value("${openapi.clientsecret}")
    private String clientSecret;

    @Value("${openapi.parking.servicekey}")
    private String park_servickey;
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
            System.out.println("Response code: " + conn.getResponseCode());
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
                if(!wait_arr.isEmpty()) {
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
                System.out.println(res);
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
            if( distance < 7.0 ) {
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


    // Trans Bank Data
    @GetMapping("/trans-bank-data")
    public void trans_map_data_method () throws IOException{
        try{
            URL url = new URL(/*URL*/"https://apis.data.go.kr/B190021/totBrStateInq/gettotBrStateInq" + "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + servicekey /*Service Key*/
            );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            System.out.println("PUBLIC_DATA_Response code: " + conn.getResponseCode());
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
            JSONObject obj = (JSONObject) parser.parse(sb.toString());
            JSONArray wait_json_arr = (JSONArray) parser.parse(obj.get("brcdList").toString());
            System.out.println("bank-data-length : "+wait_json_arr.size());
            if(wait_json_arr.isEmpty()){
                System.out.println("영업시간이 아닙니다.");
            } else {
                for(Object ob : wait_json_arr){
                    JSONObject job = (JSONObject) ob;
                    JSONObject tmp = trans_brcd(job.get("brcd").toString());
                    JSONObject coordinate = trans_geo(tmp.get("brncNwBscAdr").toString());
                    bs.preDataSave(new Bank(null,
                                            tmp.get("brcd").toString(),
                                            tmp.get("krnBrm").toString(),
                                            tmp.get("brncNwBscAdr").toString(),
                                            tmp.get("brncTelLln").toString()+"-"+tmp.get("brncTpnTon").toString()+"-"+tmp.get("brncTpnSrn").toString(),
                                            tmp.get("rprsFaxLln").toString()+"-"+tmp.get("rprsFaxTon").toString()+"-"+tmp.get("rprsFaxSrn").toString(),
                                            Double.parseDouble(coordinate.get("x").toString()),
                                            Double.parseDouble(coordinate.get("y").toString())
                                            ));
                }
                System.out.println("Success Preprocessing");
            }
        } catch ( JsonParseException je) {
            je.fillInStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    // Public_DATA_API brcd to address
    // brcd 은행지점코드를 은행지점 주소로 변환.
    private JSONObject trans_brcd(String brcd) throws IOException, ParseException {
        /*URL*/
        String brcdBuilder = "http://apis.data.go.kr/B190021/branchinfo/details" + "?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + servicekey + /*Service Key*/
                "&" + URLEncoder.encode("brcd", "UTF-8") + "=" + URLEncoder.encode(brcd, "UTF-8"); /*부점코드를 조회하고자 하는 부점의 한글명*/
        URL brcd_url = new URL(brcdBuilder);
        HttpURLConnection brcd_conn = (HttpURLConnection) brcd_url.openConnection();
        brcd_conn.setRequestMethod("GET");
        brcd_conn.setRequestProperty("Content-type", "application/json");
        BufferedReader brcd_rd;
        if(brcd_conn.getResponseCode() >= 200 && brcd_conn.getResponseCode() <= 300) {
            brcd_rd = new BufferedReader(new InputStreamReader(brcd_conn.getInputStream()));
        } else {
            brcd_rd = new BufferedReader(new InputStreamReader(brcd_conn.getErrorStream()));
        }
        StringBuilder brcd_sb = new StringBuilder();
        String brcd_line;
        while ((brcd_line = brcd_rd.readLine()) != null) {
            brcd_sb.append(brcd_line);
        }
        brcd_rd.close();
        brcd_conn.disconnect();
        JSONParser parser = new JSONParser();
        return (JSONObject)parser.parse(brcd_sb.toString());
    }

    // Naver Geocoder
    // 주소를 좌표 x,y로 변환
    private JSONObject trans_geo(String address) throws ParseException {
        JSONObject res_obj = new JSONObject();
        StringBuilder html = new StringBuilder();
        String url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode"+"?query="+URLEncoder.encode(address);// encodeURIComponent로 인코딩 된 주소
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.addHeader("X-NCP-APIGW-API-KEY-ID", clientId);  //해더에 Clinet Id와 Client Secret을 넣습니다
        request.addHeader("X-NCP-APIGW-API-KEY", clientSecret);

        try {
            HttpResponse response = client.execute(request);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
            String current = "";
            while ((current = reader.readLine()) != null) {
                html.append(current);
            }
            reader.close();
            JSONParser parser = new JSONParser();
            res_obj = (JSONObject) parser.parse(html.toString());
            JSONArray geo_arr = (JSONArray) res_obj.get("addresses");
            if (!geo_arr.isEmpty()){
                JSONObject geo_add = (JSONObject) geo_arr.get(0);
                System.out.println("geo_add : " + geo_add);
                JSONObject geo_res = new JSONObject();
                geo_res.put("x", geo_add.get("x"));
                geo_res.put("y", geo_add.get("y"));
                System.out.println("geo_res : " + geo_res);
                return geo_res;
            } else {
                JSONObject opo = new JSONObject();
                opo.put("x", "127.2115749");
                opo.put("y", "37.3456429");
                return opo;
            }
        } catch (ClientProtocolException e) {
            e.fillInStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res_obj;
    }


    @GetMapping("/trans-park-data")
    public void parking() {

        try {
            URL url = new URL("http://openapi.seoul.go.kr:8088/" + park_servickey + "/json/GetParkInfo/1/1000");
            BufferedReader bf = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            StringBuilder resultBuilder = new StringBuilder();
            String line;
            while ((line = bf.readLine()) != null) {
                resultBuilder.append(line);
            }
            String result = resultBuilder.toString();
            JSONParser jsonParser = new JSONParser();
            JSONObject json_obj = (JSONObject) jsonParser.parse(result);
            JSONObject getParkInfo = (JSONObject) json_obj.get("GetParkInfo");
            JSONArray json_arr = (JSONArray) getParkInfo.get("row");
            System.out.println("json_arr : " + json_arr.size());
            for(Object ob : json_arr) {
                JSONObject jso = (JSONObject) ob;
                // API와 미연계중인 데이터 가공 처리
                if(!jso.get("QUE_STATUS_NM").equals("미연계중")){
                    if(jso.get("PARKING_TYPE_NM") != null && Double.parseDouble(jso.get("LNG").toString()) != 0 &&
                            jso.get("QUE_STATUS_NM") != null && jso.get("OPERATION_RULE_NM")!=null &&
                            jso.get("WEEKEND_END_TIME") != null && jso.get("WEEKEND_BEGIN_TIME") != null &&
                            jso.get("WEEKEND_BEGIN_TIME") != null && jso.get("PAY_NM") != null &&
                            jso.get("SATURDAY_PAY_NM") != null && jso.get("PARKING_NAME") != null &&
                            jso.get("HOLIDAY_END_TIME") != null && jso.get("HOLIDAY_PAY_NM") != null &&
                            !jso.get("FULLTIME_MONTHLY").equals("") && jso.get("ADDR") != null &&
                            jso.get("PARKING_CODE") != null && jso.get("HOLIDAY_BEGIN_TIME") != null &&
                            jso.get("HOLIDAY_PAY_YN") != null && !jso.get("TEL").equals("") &&
                            jso.get("NIGHT_FREE_OPEN_NM") != null && Double.parseDouble(jso.get("LAT").toString()) != 0 &&
                            jso.get("SATURDAY_PAY_YN") != null && jso.get("HOLIDAY_PAY_YN") != null) {
                        System.out.println(jso);
                        Park tmp = new Park();
                        tmp.setType(jso.get("PARKING_TYPE_NM").toString());
                        tmp.setLat(Double.parseDouble(jso.get("LAT").toString()));
                        tmp.setLng(Double.parseDouble(jso.get("LNG").toString()));
                        tmp.setPkname(jso.get("PARKING_NAME").toString());
                        tmp.setPkrule(jso.get("OPERATION_RULE_NM").toString());
                        tmp.setCapacity(Integer.parseInt(jso.get("CAPACITY").toString().substring(0,jso.get("CAPACITY").toString().indexOf("."))));
                        tmp.setPaytype(jso.get("PAY_NM").toString());
                        tmp.setHolidaytime(jso.get("HOLIDAY_BEGIN_TIME").toString()+"~"+jso.get("HOLIDAY_END_TIME").toString());
                        tmp.setHolidaypaytype(jso.get("HOLIDAY_PAY_NM").toString());
                        tmp.setWeekdaytime(jso.get("WEEKDAY_BEGIN_TIME").toString()+"~"+jso.get("WEEKDAY_END_TIME").toString());
                        tmp.setWeekendtime(jso.get("WEEKEND_BEGIN_TIME").toString()+"~"+jso.get("WEEKEND_END_TIME").toString());
                        tmp.setSaturdaypay(jso.get("SATURDAY_PAY_NM").toString());
                        tmp.setRates(jso.get("RATES").toString());
                        tmp.setTimerates(jso.get("TIME_RATE").toString());
                        tmp.setAddrates(jso.get("ADD_RATES").toString());
                        tmp.setAddtimerates(jso.get("ADD_TIME_RATE").toString());
                        tmp.setDaymaximum(jso.get("DAY_MAXIMUM").toString());
                        tmp.setFullmonthly(Integer.parseInt(jso.get("FULLTIME_MONTHLY").toString()));
                        tmp.setPkaddr(jso.get("ADDR").toString());
                        tmp.setPkcode(jso.get("PARKING_CODE").toString());
                        tmp.setTel(jso.get("TEL").toString());
                        tmp.setNightyn(jso.get("NIGHT_FREE_OPEN_NM").toString());
                        ps.insertParking(tmp);
                    } else {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }

    }
}
