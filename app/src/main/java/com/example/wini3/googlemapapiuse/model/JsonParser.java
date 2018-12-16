package com.example.wini3.googlemapapiuse.model;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

//import com.google.android.gms.common.util.IOUtils;

@SuppressWarnings("ALL")
public class JsonParser {

    private static InputStream is;
    private static JSONObject jObj;
    private static String json = "";


    public JsonParser() {
    }

    public JSONObject makeHttpRequest(String url, String method, List params) {
        try {
            if(method.equals("POST"))
                postMethod(url, method, params);
            if(method.equals("GET"))
                getMethod(url, method, params);

            byte[] isBytes = toByteArray(is);

            String stringByte = new String(isBytes, "utf-8");
            jObj = new JSONObject(stringByte);
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("ExceptionERROR", e.getMessage());
        }
        return jObj;
//        return null;
    }

    private byte[] toByteArray(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            int nRead;
            byte[] data = new byte[1048576];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException ex) {

        }
        return buffer.toByteArray();
    }

    private void postMethod(String url, String method, List params) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        is = httpEntity.getContent();
    }

    private void getMethod(String url, String method, List params) throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String paramString = URLEncodedUtils.format(params, "utf-8");
//        url += "?" + paramString;
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        is = httpEntity.getContent();
    }

}
