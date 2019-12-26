
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Httptest {
    //查询霸王餐列表url
    private static final String QUERY_LIST = "http://m.dianping.com/activity/static/pc/ajaxList";

    //报名霸王餐url
    private static final String SIGN_UP = "http://s.dianping.com/ajax/json/activity/offline/saveApplyInfo";

    //登录二维码url
    private static final String LOGIN_IMAGE = "https://account.dianping.com/account/getqrcodeimg";

    //检测登录url
    private static final String LOGIN_STATUS = "https://account.dianping.com/account/ajax/queryqrcodestatus";

    //post参数格式--json
    private static final String JSON_TYPE = "json";

    //post参数格式--键值对
    private static final String WWW_TYPE = "x-www-form-urlencoded";

    //cookie容器
    private static HttpClientContext context = HttpClientContext.create();

    public static void main(String[] args) throws IOException, InterruptedException {
        //1、访问登录二维码
        sendHttp(LOGIN_IMAGE, null, JSON_TYPE);

        //2、检测是否已经扫码
        boolean flag = true;
        while ( flag ){
            String success = sendHttp(LOGIN_STATUS,buildLoginParam(),WWW_TYPE);
            JSONObject jsonObject = JSONObject.parseObject(success);
            JSONObject jsonObject1 = jsonObject.getJSONObject("msg");
            String status = jsonObject1.getString("status");
            if ("2".equals(status)){
                flag = false;
            }
            Thread.sleep(5000);
        }

        //3、进行报名
        System.out.println("开始进行霸王餐报名！！");
        int num = 0;
        for (int i = 1; i < 10; i++) {

            //3.1、查询出所有霸王餐（只查电子券）
            String queryList =
                    sendHttp(QUERY_LIST, buildQueryListData(i), JSON_TYPE);

            //3.2、筛选出多人餐
            JSONObject jsonObject = JSONObject.parseObject(queryList);
            JSONObject jsonObject1 = jsonObject.getJSONObject("data");
            JSONArray jsonArray = jsonObject1.getJSONArray("detail");
            if(jsonArray == null || jsonArray.size() == 0){
                break;
            }
            List<ItemVo> lists = new ArrayList<ItemVo>();
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONObject item = (JSONObject) jsonArray.get(j);
                String activName = item.getString("activityTitle");
                if (activName.contains("套餐")){
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
                String signResult = sendHttp(SIGN_UP, buildSignUpData(itemVo.getId()),WWW_TYPE);
                JSONObject result = JSONObject.parseObject(signResult);
                JSONObject signMsg = result.getJSONObject("msg");
                String html = signMsg.getString("html");
                if (html.length()>30 && html.contains("报名成功")){
                    System.out.println("报名成功");
                } else {
                    System.out.println(signMsg.getString("html"));
                }
                num++;
                Thread.sleep(1000);
            }

        }
        System.out.println("****已经报完所有霸王餐****共计"+num+"家");
    }

    /**
     * 组织登录参数
     * @return
     * @throws UnsupportedEncodingException
     */
    private static HttpEntity buildLoginParam() throws UnsupportedEncodingException {
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        if (null != context.getCookieStore()){
            List<Cookie> cookies = context.getCookieStore().getCookies();
            for (Cookie c:cookies
            ) {
                NameValuePair value = new BasicNameValuePair(c.getName(),c.getValue());
                list.add(value);
            }
        }
        return new UrlEncodedFormEntity(list,"utf-8");
    }

    private static HttpEntity buildQueryListData(int i) {
        //type=1 美食  mode=3 电子券
        String json = "{\"cityId\":\"2\",\"type\":1,\"mode\":\"3\",\"page\":"+i+"}";
        return new StringEntity(json,"utf-8");
    }

    /**
     * 组织报名参数
     * @param id
     * @return
     * @throws UnsupportedEncodingException
     */
    private static HttpEntity buildSignUpData(String id) throws UnsupportedEncodingException {
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        NameValuePair value1 = new BasicNameValuePair("isShareQQ","false");
        NameValuePair value2 = new BasicNameValuePair("isShareSina","false");
        NameValuePair value3 = new BasicNameValuePair("marryStatus","0");
        NameValuePair value4 = new BasicNameValuePair("offlineActivityId",id);
        //不清楚手机号的机制，是否可以去掉
        NameValuePair value5 = new BasicNameValuePair("phoneNo","18734835109");
        NameValuePair value6 = new BasicNameValuePair("usePassCard","0");
        list.add(value1);
        list.add(value2);
        list.add(value3);
        list.add(value4);
//        list.add(value5);
        list.add(value6);
        return new UrlEncodedFormEntity(list,"utf-8");
    }

    /**
     * 发送http请求
     * @param url
     * @param stringEntity
     * @param postType
     * @return
     * @throws IOException
     */
    private static String sendHttp(String url, HttpEntity stringEntity, String postType) throws IOException {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(stringEntity);
        //设置cookie类型，否则无法set-cookie
        RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        httpPost.setConfig(defaultConfig);
        //设置常规请求头
        httpPost.addHeader("Accept","application/json, text/javascript, */*; q=0.01");
        httpPost.addHeader("Origin","http://s.dianping.com");
        httpPost.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
        httpPost.addHeader("Content-Type","application/" + postType);
        CloseableHttpResponse closeableHttpResponse = null;
        String response = "";
        try {
            closeableHttpResponse = closeableHttpClient.execute(httpPost,context);
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            //登录特殊处理，下载并显示二维码
            if (LOGIN_IMAGE.equals(url)){
                //显示登录二维码
                InputStream inputStream = httpEntity.getContent();
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                int width = bufferedImage.getWidth(null);
                int heigh = bufferedImage.getHeight(null);
                JLabel jLabel = new JLabel(new ImageIcon(bufferedImage));
                JFrame jFrame = new JFrame();
                jFrame.getContentPane().add(jLabel, BorderLayout.CENTER);
                jFrame.setSize(width+100,heigh+100);
                jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                jFrame.setLocationRelativeTo(null);
                jFrame.setVisible(true);
                jFrame.setTitle("大众点评APP扫码登录");
            } else {
                response = EntityUtils.toString(httpEntity,"utf-8");
//                System.out.println(response);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            closeableHttpResponse.close();
        }
        return response;
    }

    /**
     * 输出当前环境cookie，调试使用
     * @return
     */
    private static String getCookies() {
        String cookie = "";
        if (null != context.getCookieStore()){
            List<Cookie> cookies = context.getCookieStore().getCookies();
            for (Cookie c:cookies
                 ) {
                cookie += (c.getName()+"="+c.getValue()+"; ");
            }
        }
        System.out.println(cookie);
        return cookie;
    }

}
