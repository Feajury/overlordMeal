
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Httptest {
    //查询霸王餐列表url
    private static final String QUERY_LIST = "http://m.dianping.com/activity/static/pc/ajaxList";

    //报名霸王餐url
    private static final String SIGN_UP= "http://s.dianping.com/ajax/json/activity/offline/saveApplyInfo";

    //post参数格式--json
    private static final String JSON_TYPE = "json";

    //post参数格式--键值对
    private static final String WWW_TYPE = "x-www-form-urlencoded";

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("开始进行霸王餐报名！！");
        int num = 0;
        for (int i = 2; i < 3; i++) {

            //1、查询出所有霸王餐（只查电子券）
            String queryList =
                    sendHttp(QUERY_LIST, buildQueryListData(i), JSON_TYPE);

            //2、筛选出多人餐
            JSONObject jsonObject = JSONObject.parseObject(queryList);
            JSONObject jsonObject1 = jsonObject.getJSONObject("data");
            JSONArray jsonArray = jsonObject1.getJSONArray("detail");
            if(jsonArray == null || jsonArray.size() == 0){
                return;
            }
            List<ItemVo> lists = new ArrayList<ItemVo>();
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject item = (JSONObject) jsonArray.get(j);
                String activName = item.getString("activityTitle");
                if (activName.contains("人餐")){
                    ItemVo itemVo = new ItemVo();
                    itemVo.setId(item.getString("offlineActivityId"));
                    itemVo.setName(item.getString("activityTitle"));
                    itemVo.setLocation(item.getString("regionName"));
                    lists.add(itemVo);
                }
            }

            //3、循环报名
            for (ItemVo itemVo: lists
                 ) {
                System.out.println(itemVo.getName()+"/"+itemVo.getLocation());
                String response = sendHttp(SIGN_UP, buildSignUpData(itemVo.getId()),WWW_TYPE);
                JSONObject resp = JSONObject.parseObject(response);
                String flag = resp.getString("code");
                if ("200".equals(flag)){
                    System.out.println("报名成功");
                } else {
                    System.out.println("已经报过了");
                }
                num++;
                Thread.sleep(2000);
            }

        }
        System.out.println("****已经报完所有霸王餐****共计"+num+"家");
    }

    private static HttpEntity buildQueryListData(int i) {
        //type=1 美食  mode=3 电子券
        String json = "{\"cityId\":\"2\",\"type\":1,\"mode\":\"3\",\"page\":"+i+"}";
        return new StringEntity(json,"utf-8");
    }

    private static HttpEntity buildSignUpData(String id) throws UnsupportedEncodingException {
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        NameValuePair value1 = new BasicNameValuePair("isShareQQ","false");
        NameValuePair value2 = new BasicNameValuePair("isShareSina","false");
        NameValuePair value3 = new BasicNameValuePair("marryStatus","0");
        NameValuePair value4 = new BasicNameValuePair("offlineActivityId",id);
        NameValuePair value5 = new BasicNameValuePair("phoneNo","187****5109");
        NameValuePair value6 = new BasicNameValuePair("usePassCard","0");
        list.add(value1);
        list.add(value2);
        list.add(value3);
        list.add(value4);
        list.add(value5);
        list.add(value6);
        return new UrlEncodedFormEntity(list,"utf-8");
    }

    private static String sendHttp(String url, HttpEntity stringEntity, String postType) throws IOException {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(stringEntity);
        httpPost.addHeader("Accept","application/json, text/javascript, */*; q=0.01");
        httpPost.addHeader("Origin","http://s.dianping.com");
        httpPost.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
        httpPost.addHeader("Content-Type","application/" + postType);
        httpPost.addHeader("Cookie","_lxsdk_cuid=16e90fefb32c8-05e6985f6db227-b363e65-e1000-16e90fefb32c8; _lxsdk=16e90fefb32c8-05e6985f6db227-b363e65-e1000-16e90fefb32c8; _hc.v=9f5d5d9a-40a2-68c7-37b1-443855ee7058.1574390661; cye=beijing; ua=%E6%AE%87%E7%BE%BD%E9%BB%98; ctu=e738398a01987d9351b1d2fca55d908b38f09dedd67aefc6f7962a6623a635e7; s_ViewType=10; cy=2; _lx_utm=utm_source%3Dgoogle%26utm_medium%3Dorganic; lgtoken=00b6537db-85a4-4186-a164-21e1d4c3ba06; dper=b8bc950b65b0f2d44b56c74e5555dc92ddf7c40c6d96de5c29485abe8243354b86388c3ce1cd99e75592d1384f3c73b1c123674789b2b931a95ab187446da922f4221eaf0f36c99f68f5e86b76dd0f7bb9e47457241eda217665a2fcc01556d5; ll=7fd06e815b796be3df069dec7836c3df; _lxsdk_s=16f3581dc31-ed8-42b-125%7C%7C19");
        CloseableHttpResponse closeableHttpResponse = null;
        String response = "";
        try {
            closeableHttpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            response = EntityUtils.toString(httpEntity,"utf-8");
//            System.out.println(response);
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            closeableHttpResponse.close();
        }
        return response;
    }
}
