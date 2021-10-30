import com.alibaba.fastjson.JSON;import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import okhttp3.*;

import javax.print.attribute.standard.Media;
import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class TestOKHttp {
    public static String url="http://192.168.2.2:27739";
    public void post(String url){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject=new JSONObject();
        //jsonObject.put("cmd","MoveToCenterByPoint");
        //jsonObject.put("cmd","SdkGetMasterMode");
        //jsonObject.put("cmd","SdkGetPitchCheckerKeepSeconds");
        //jsonObject.put("x","0.5");
        //jsonObject.put("y","0.9");
        /*
        设置速度模式
         */
        jsonObject.put("cmd","SdkSetSpeedMode");
        jsonObject.put("mode","3");
        /*
        AI功能配置选项
         */
        jsonObject.put("cmd","SdkSetConfig");
        jsonObject.put("key","5");
        jsonObject.put("val","0");
        System.out.println(jsonObject);
        //Map<String,Object> pp=JSONObject.parseObject(jsonObject.toJSONString());
        String json=jsonObject.toString();
        MediaType JSON=MediaType.parse("application/json;charset=utf-8");
        RequestBody formBody=RequestBody.create(JSON,json);
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public DataAll get(){
        String url="http://192.168.188.1/api/v1/system/firmware";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                //.header("host","192.168.188.1")
                .header("Content_Type","application/json")
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().string());
            //String re=response.body().string();
            /*JSONObject jsonObject = new JsonParser().parse(re).getAsJsonObject();
            JSONObject jsonArray = jsonObject.getJSONObject("data");
            String content = "";
            content = jsonArray.toString();
            return new Gson().fromJson(content, DataAll.class);*/
            //return new Gson()
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }
    public static void main(String[] args) {
       /* TestOKHttp testOKHttp=new TestOKHttp();
        Scanner scanner=new Scanner(System.in);
        String temp=scanner.nextLine();
        testOKHttp.post(url+temp);*/
        //testOKHttp.get(url+temp);
        TestOKHttp testOKHttp=new TestOKHttp();
        DataAll dataAll= testOKHttp.get();
        System.out.println("我是傻逼");
        System.out.println("我是麻瓜");

    }


}
