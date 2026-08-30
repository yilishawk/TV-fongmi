package com.github.catvod.spider;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JP extends Spider {
    
    private static final String HOST = "https://m.9zhoukj.com";
    
    @Override
    public void init(Context context) {
        SpiderDebug.log("[JP] init");
    }
    
    private String fetch(String url, Map<String, String> headers) throws Exception {
        return OkHttp.string(url, headers);
    }
    
    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();
            
            String[][] classArr = {
                {"2_14", "国产剧"}, {"2_15", "欧美剧"}, {"2_62", "日韩剧"},
                {"2_16", "港台剧"}, {"1_", "电影"}, {"3_", "综艺"}
            };
            for (String[] c : classArr) {
                JSONObject cls = new JSONObject();
                cls.put("type_id", c[0]);
                cls.put("type_name", c[1]);
                classes.put(cls);
            }
            result.put("class", classes);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "{}";
        }
    }
    
    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            String params = "type1=" + tid.split("_")[0] + "&pageNum=" + pg + "&pageSize=30";
            String json = fetch(HOST + "/api/mw-movie/anonymous/video/list?" + params, null);
            
            JSONObject root = new JSONObject(json);
            if (root.optInt("code") != 0) {
                return "{\"list\":[]}";
            }
            
            JSONObject data = root.optJSONObject("data");
            JSONArray list = data != null ? data.optJSONArray("list") : new JSONArray();
            JSONArray videos = new JSONArray();
            
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                JSONObject vod = new JSONObject();
                vod.put("vod_id", item.optString("vodId"));
                vod.put("vod_name", item.optString("vodName"));
                videos.put(vod);
            }
            
            JSONObject result = new JSONObject();
            result.put("list", videos);
            result.put("pagecount", data != null ? data.optInt("totalPage", 1) : 1);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "{\"list\":[]}";
        }
    }
    
    @Override
    public String detailContent(List<String> ids) {
        try {
            String id = ids.get(0);
            String json = fetch(HOST + "/api/mw-movie/anonymous/video/detail?id=" + id, null);
            
            JSONObject data = new JSONObject(json).optJSONObject("data");
            if (data == null) return "{\"list\":[]}";
            
            JSONObject vod = new JSONObject();
            vod.put("vod_id", data.optString("vodId", id));
            vod.put("vod_name", data.optString("vodName", ""));
            vod.put("vod_pic", data.optString("vodPic", ""));
            
            JSONArray episodes = data.optJSONArray("episodeList");
            List<String> epList = new java.util.ArrayList<>();
            if (episodes != null) {
                for (int i = 0; i < episodes.length(); i++) {
                    JSONObject ep = episodes.getJSONObject(i);
                    epList.add(ep.optString("name") + "$" + id + "@@" + ep.optString("nid"));
                }
            }
            
            vod.put("vod_play_from", "九州空间");
            vod.put("vod_play_url", String.join("#", epList));
            
            JSONArray list = new JSONArray();
            list.put(vod);
            return "{\"list\":" + list.toString() + "}";
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "{\"list\":[]}";
        }
    }
    
    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String params = "keyword=" + key + "&pageNum=1&pageSize=30";
            String json = fetch(HOST + "/api/mw-movie/anonymous/video/searchByWord?" + params, null);
            
            JSONObject root = new JSONObject(json);
            JSONObject data = root.optJSONObject("data");
            JSONObject resultObj = data != null ? data.optJSONObject("result") : null;
            JSONArray rawList = resultObj != null ? resultObj.optJSONArray("list") : new JSONArray();
            
            JSONArray videos = new JSONArray();
            for (int i = 0; i < rawList.length(); i++) {
                JSONObject item = rawList.getJSONObject(i);
                JSONObject vod = new JSONObject();
                vod.put("vod_id", item.optString("vodId"));
                vod.put("vod_name", item.optString("vodName"));
                videos.put(vod);
            }
            
            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "{\"list\":[]}";
        }
    }
    
    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            String[] parts = id.split("@@");
            if (parts.length < 2) return "{\"parse\":1,\"url\":\"" + id + "\"}";
            
            String params = "clientType=3&id=" + parts[0] + "&nid=" + parts[1];
            String json = fetch(HOST + "/api/mw-movie/anonymous/v2/video/episode/url?" + params, null);
            
            JSONObject data = new JSONObject(json).optJSONObject("data");
            JSONArray list = data != null ? data.optJSONArray("list") : new JSONArray();
            
            for (int i = 0; i < list.length(); i++) {
                JSONObject v = list.getJSONObject(i);
                if (v.optInt("resolution") == 1080) {
                    JSONObject result = new JSONObject();
                    result.put("parse", 0);
                    result.put("url", v.optString("url"));
                    return result.toString();
                }
            }
            
            if (list.length() > 0) {
                JSONObject result = new JSONObject();
                result.put("parse", 0);
                result.put("url", list.getJSONObject(0).optString("url"));
                return result.toString();
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "{\"parse\":1,\"url\":\"" + id + "\"}";
    }
}
