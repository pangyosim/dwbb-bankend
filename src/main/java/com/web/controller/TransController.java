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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@PropertySource("classpath:application.properties")
public class TransController {
    private final BankService bs;
    private final ParkService ps;
    @Value("${openapi.clientid}")
    private String clientId;
    @Value("${openapi.clientsecret}")
    private String clientSecret;
    @Value("${openapi.servicekey}")
    private String servicekey;
    @Value("${openapi.parking.servicekey}")
    private String park_servickey;
    public TransController(BankService bs, ParkService ps) {
        this.bs = bs;
        this.ps = ps;
    }
    // Trans Bank Data
    @GetMapping("/trans-bank-data")
    public void trans_map_data_method () throws IOException {
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
