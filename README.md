<br></br>
### 💁🏻 <a href="https://server.dwbb.kro.kr"> &nbsp;사이트로 이동</a><br></br>

### 🔗 <a href="https://github.com/pangyosim/dwbb-frontend"> &nbsp; Frontend Github</a>

<br></br>
# 1.&nbsp; Springboot Controller
#### ✅ &nbsp; MapController
- Naver Maps에 Marker로 표시하기 위해 주소값을 좌표값으로 변경 후 DB에 데이터 주입
- 순서: 공공데이터 API → Naver Geolocation API → DB저장 <br>
    🟢 공공데이터 API 업데이트에 따라 맵에 표시되는 데이터도 다르므로, DB데이터보다 상시 요청이 더 낫다 판단하여 상시 API 데이터 요청.
- request & response : Frontend(React)에서 현재 좌표값 요청시 Backend(Springboot)에서 내 근처 7km 이내 은행 대기인원 현황으로 응답.
- key : application.properties 로 관리. <br>

🚨 API SERVER ERROR 발생시 : try/catch로 jsonparse exception 발생시켜 서버에러값 응답.

```java
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
```

#### ✅ &nbsp; NoticeController
- 공지사항, 공지사항 상세 페이지, 공지사항 등록페이지
- USER : 공지사항 조회만 가능
- ADMIN : 공지사항 등록, 삭제 가능.
  
```java
package com.web.controller;

import com.web.repo.Notice;
import com.web.repo.QnA;
import com.web.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
@CrossOrigin("https://dwbb.vercel.app/")
@RestController
public class NoticeController {
    private final NoticeService ns;

    @Autowired
    public NoticeController(NoticeService ns) {
        this.ns = ns;
    }

    @PostMapping("/notice-all")
    @CrossOrigin
    public List<Notice> noticeall_method (){
        try{
            return ns.findAll();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return null;
    }

    @PostMapping("/notice-views")
    @CrossOrigin
    public int update_views_method(@RequestBody Notice notice){
        try{
            return ns.updateviewsByseq(notice.getNoticeseq());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return -1;
    }

    @PostMapping("/notice-register")
    @CrossOrigin
    public String register_notice_method(@RequestBody Notice notice){
        try {
            notice.setNoticecreateday(new Date());
            notice.setNoticefile("C:\\TESTDATAVOLUME");
            ns.noticeRegister(notice);
            return "register-success";
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return "";
    }

    @PostMapping("/notice-delete")
    @CrossOrigin
    public String delete_notice_method(@RequestBody Notice notice){
        ns.noticeDelete(notice);
        return "delete-success";
    }
}

```

#### ✅ &nbsp; QnAController
- QnA , QnA 상세페이지, QnA등록페이지
- 구성 : 공지사항 3row + QnA 7row 
- USER : QnA등록, 본인이 작성한 글만 삭제 가능.
- ADMIN : QnA등록, 모든 사람이 작성한 글 삭제 가능.

```java
package com.web.controller;

import com.web.repo.Notice;
import com.web.repo.QnA;
import com.web.service.QnAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
@CrossOrigin("https://www.dwbb.online/")
@RestController
public class QnAController {

    private final QnAService qas;

    @Autowired
    public QnAController(QnAService qas) {
        this.qas = qas;
    }

    @PostMapping("/qna-all")
    @CrossOrigin
    public List<QnA> qnaall_method (){
        try {
            return qas.findAll();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return null;
    }

    @PostMapping("/qna-views")
    @CrossOrigin
    public int update_views_method(@RequestBody QnA qna){
        try{
            return qas.updateviewsByseq(qna.getQnaseq());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return -1;
    }

    @PostMapping("/qna-register")
    @CrossOrigin
    public String register_qna_method(@RequestBody QnA qna){
        qna.setQnacreateday(new Date());
        qas.qnaRegister(qna);
        return "register-success";
    }

    @PostMapping("/qna-delete")
    @CrossOrigin
    public String delete_qna_method(@RequestBody QnA qna){
        qas.deleteqna(qna);
        return "delete-success";
    }

    @PostMapping("/qna-comments")
    @CrossOrigin
    public String comments_qna_method(@RequestBody QnA qna){
        System.out.println(qna);
        qas.updatecommentsByseq(qna.getQnaseq(), qna.getComments());
        return "comments-success";
    }
}

```

#### ✅ &nbsp; UserController
- 로그인, 회원가입, 아이디/비밀번호찾기 페이지
- 로그인 : Frontend 에서 Client가 입력한 값들 검증 후 성공할 시 Username을 Hash256 사용하여 JWT 토큰화 후 Frontend로 전달.
- 회원가입 : Frontend에서 입력값 검증 후 DB에 저장.
- 아이디/비밀번호찾기 : Frontend에서 검증된 이메일로 DB에서 where절로 확인.
  
```java
package com.web.controller;

import com.web.service.MailService;
import com.web.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import com.web.repo.*;
@CrossOrigin("https://www.dwbb.online/")
@RestController
@PropertySource("classpath:application.properties")
public class UserController {

    @Value("${user.jwtkey}")
    private String securityKey;
    private final Long expiredTime = 1000 * 60L * 60L * 3L;
    private final UserService us;
    private final MailService ms;
    @Autowired
    public UserController(UserService us,MailService ms) {
        this.us = us;
        this.ms = ms;
    }

    @PostMapping("/check-login")
    @CrossOrigin
    public String[] loginMethod (@RequestBody User usr){
        try {
            Date now = new Date();
            User login_result = us.loginCheckByIdPw(usr.getId(),usr.getPw());
            System.out.println(login_result);
            if( login_result != null) {
                String[] res_arr = new String[3];
                res_arr[0] = login_result.getNickname();
                res_arr[1] = login_result.getRole();
                res_arr[2] = Jwts.builder()
                                .setSubject(login_result.getNickname())
                                .setHeader(createHeader())
                                .setClaims(createClaims(login_result))
                                .setExpiration(new Date(now.getTime()+expiredTime))
                                .signWith(SignatureAlgorithm.HS256,securityKey)
                                .compact();
                return res_arr;
            }
        } catch (Exception e) {
                e.fillInStackTrace();
        }
        return null;
    }
    @PostMapping("/signup")
    @CrossOrigin
    public String signup (@RequestBody User usr){
        us.signup(usr);
        return "signupsuccess";
    }

    @PostMapping("/check-email")
    @CrossOrigin
    public User checkemailMethod (@RequestBody User usr){
        try{
            User finduser = us.findUserByEmail(usr.getEmail());
            if(finduser != null) {
                int i = ms.sendMail(usr.getEmail());
                finduser.setSeq(Long.parseLong(Integer.toString(i)));
                return finduser;
            } else {
                User find_fail = new User();
                find_fail.setEmail("findfail");
                return find_fail;
            }
        } catch (Exception e){
            e.fillInStackTrace();
        }
        return null;
    }

    @PostMapping("/distinct-email")
    @CrossOrigin
    public String checkdistinctemail (@RequestBody User usr){
        try{
            User distinct_check = us.findUserByEmail(usr.getEmail());
            if( distinct_check != null){
                return "distinct";
            } else {
                return "available";
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return  "";
    }

    private Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("type", "JWT");
        header.put("alg", "HS256"); // 해시 256 사용하여 암호화
        header.put("regDate", System.currentTimeMillis());
        return header;
    }
    private Map<String, Object> createClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("pw", user.getPw()); // username
        claims.put("roles", user.getRole()); // 인가정보
        return claims;
    }
}
```


