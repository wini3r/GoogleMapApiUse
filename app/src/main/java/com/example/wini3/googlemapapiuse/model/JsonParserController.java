package com.example.wini3.googlemapapiuse.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class JsonParserController {

    private JsonParser jsonParser = new JsonParser();
//    private ArrayList<HashMap<String, String>> locatinsMapList = new ArrayList<>();
    private JSONArray jsonLocations = new JSONArray();
    private ArrayList<PointLocation> pointLocations;

    public static final String URL_UPDATE_LOCATION = "http://8ps71soo.zzz.com.ua/update_location.php";
    public static final String URL_ALL_LOCATIONS =  "http://8ps71soo.zzz.com.ua/select_location.php";
    public static final String TAG_SECCESS = "success";
    public static final String TAG_LOCATIONS = "locations";
    public static final String TAG_ID = "id";
    public static final String TAG_NAME = "name";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LON = "lon";
    public static final String TAG_DESCRIPTION = "description";
    public static final String TAG_UUID = "uuid";

    public JsonParserController() {
    }

    public ArrayList<PointLocation> request() {
        JSONObject jsonObject = jsonParser.makeHttpRequest(URL_ALL_LOCATIONS, "GET", new ArrayList<>());
        return jsonToPointArray(jsonObject);
    }

    private ArrayList<PointLocation> jsonToPointArray(JSONObject jsonObject) {
        pointLocations = new ArrayList<>();
        try {
            int seccess = jsonObject.getInt(TAG_SECCESS);
            if (seccess == 0) return null;
            jsonLocations = jsonObject.getJSONArray(TAG_LOCATIONS);
            for (int i = 0; i < jsonLocations.length(); i++) {
                JSONObject currentJson = jsonLocations.getJSONObject(i);
                PointLocation point = jsonToPoint(currentJson);
                pointLocations.add(point);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pointLocations;
    }

    private PointLocation jsonToPoint(JSONObject json) throws JSONException {
        PointLocation pointLocation = new PointLocation();
        pointLocation.setId(json.getString(TAG_ID));
        pointLocation.setName(json.getString(TAG_NAME));
        pointLocation.setLat(json.getDouble(TAG_LAT));
        pointLocation.setLon(json.getDouble(TAG_LON));
        pointLocation.setComment(json.getString(TAG_DESCRIPTION));
        return pointLocation;
    }


    public ArrayList<PointLocation> requestUpdate(List params) {
        pointLocations = new ArrayList<>();
        JSONObject jsonObject = jsonParser.makeHttpRequest(URL_UPDATE_LOCATION, "POST", params);
        return jsonToPointArray(jsonObject);
    }
}
