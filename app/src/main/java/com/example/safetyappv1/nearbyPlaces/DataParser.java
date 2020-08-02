package com.example.safetyappv1.nearbyPlaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser
{
    private HashMap<String,String> getSingleNearbyPlace(JSONObject googlePlaceJson)
    {
        HashMap<String,String> googlePlaceMap = new HashMap<>();
        String NameOfPlace = "-NA-";
        String Vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try {
            if(!googlePlaceJson.isNull("name"))
            {
                NameOfPlace = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("Vicinity"))
            {
                Vicinity = googlePlaceJson.getString("Vicinity");
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("Location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("Location").getString("lng");
            reference = googlePlaceJson.getString("reference");

            googlePlaceMap.put("place_name",NameOfPlace);
            googlePlaceMap.put("Vicinity",Vicinity);
            googlePlaceMap.put("Lat",latitude);
            googlePlaceMap.put("Lng",longitude);
            googlePlaceMap.put("reference",reference);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;
    }

    private List<HashMap<String,String>> getAllNearbyPlaces(JSONArray jsonArray)
    {
        int counter = jsonArray.length();

        List<HashMap<String,String>> NearByPlacesList = new ArrayList<>();

        HashMap<String,String> NearByPlaceMap = null;

        for(int i = 0 ;i <counter; i++)
        {
            try {
                NearByPlaceMap = getSingleNearbyPlace((JSONObject) jsonArray.get(i));
                NearByPlacesList.add(NearByPlaceMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return NearByPlacesList;
    }

    public List<HashMap<String,String>> parse(String jSONdata)
    {
        JSONArray jsonArray = null;

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jSONdata);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getAllNearbyPlaces(jsonArray);
    }
}
