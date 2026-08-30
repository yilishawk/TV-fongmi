package com.github.catvod.spider;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NM extends Spider {
    
    private static final String SITE_URL = "https://vip.wwgz.cn:5200";
    private static final String API_HOST = "https://api.wwgz.cn:520";
    private static final String UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36";
    
    @Override
    public void init(Context context) {
        SpiderDebug.log("[NM] init");
    }
    
    private String fetch(String url) throws Exception {
        return OkHttp.string(url, null);
    }
    
    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();
            
            String[][] classArr = {
                {"12", "国产剧"}, {"1", "电影"}, {"2", "电视剧"}, {"3", "综艺"}, {"26", "短剧"}
            };
            for (String[] c : classArr) {
                JSONObject obj = new JSONObject();
                obj.put("type_id", c[0]);
                obj.put("type_name", c[1]);
                classes.put(obj);
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
            String url = SITE_URL + "/vod-list-id-" + tid + "-pg-" + pg + ".html";
            String html = fetch(url);
            
            JSONArray videos = new JSONArray();
            int start = 0;
            while (start < html.length()) {
                int itemStart = html.indexOf("<li", start);
                if (itemStart == -1) break;
                int itemEnd = html.indexOf("</li>", itemStart);
                if (itemEnd == -1) break;
                
                String item = html.substring(itemStart, itemEnd);
                JSONObject vod = new JSONObject();
                
                // Extract id from href
                int hrefStart = item.indexOf("href=\"");
                if (hrefStart != -1) {
                    hrefStart += 6;
                    int hrefEnd = item.indexOf("\"", hrefStart);
                    if (hrefEnd != -1) {
                        String href = item.substring(hrefStart, hrefEnd);
                        if (href.contains("/vod-detail-id-")) {
                            int idStart = href.indexOf("-id-") + 4;
                            int idEnd = href.indexOf(".html", idStart);
                            if (idEnd != -1) {
                                vod.put("vod_id", href.substring(idStart, idEnd));
                            }
                        }
                    }
                }
                
                // Extract name
                int titleStart = item.indexOf(">");
                if (titleStart != -1) {
                    int titleEnd = item.indexOf("</a>", titleStart);
                    if (titleEnd == -1) titleEnd = item.indexOf("<", titleStart);
                    if (titleEnd != -1) {
                        vod.put("vod_name", item.substring(titleStart + 1, titleEnd).trim());
                    }
                }
                
                if (vod.length() > 0) videos.put(vod);
                start = itemEnd;
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
    public String detailContent(List<String> ids) {
        try {
            String id = ids.get(0);
            String url = SITE_URL + "/vod-detail-id-" + id + ".html";
            String html = fetch(url);
            
            JSONObject vod = new JSONObject();
            vod.put("vod_id", id);
            
            // Extract title
            int titleStart = html.indexOf("<h1");
            if (titleStart != -1) {
                int titleEnd = html.indexOf("</h1>", titleStart);
                if (titleEnd != -1) {
                    vod.put("vod_name", html.substring(titleStart, titleEnd).replaceAll("<[^>]+>", "").trim());
                }
            }
            
            JSONArray list = new JSONArray();
            list.put(vod);
            JSONObject result = new JSONObject();
            result.put("list", list);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "{\"list\":[]}";
        }
    }
    
    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String url = SITE_URL + "/vod-search-pg-1-wd-" + URLEncoder.encode(key, "UTF-8") + ".html";
            String html = fetch(url);
            
            JSONArray videos = new JSONArray();
            int start = 0;
            while (start < html.length()) {
                int itemStart = html.indexOf("<li", start);
                if (itemStart == -1) break;
                int itemEnd = html.indexOf("</li>", itemStart);
                if (itemEnd == -1) break;
                
                String item = html.substring(itemStart, itemEnd);
                JSONObject vod = new JSONObject();
                
                int hrefStart = item.indexOf("href=\"");
                if (hrefStart != -1) {
                    hrefStart += 6;
                    int hrefEnd = item.indexOf("\"", hrefStart);
                    if (hrefEnd != -1) {
                        String href = item.substring(hrefStart, hrefEnd);
                        if (href.contains("/vod-detail-id-")) {
                            int idStart = href.indexOf("-id-") + 4;
                            int idEnd = href.indexOf(".html", idStart);
                            if (idEnd != -1) {
                                vod.put("vod_id", href.substring(idStart, idEnd));
                            }
                        }
                    }
                }
                
                if (vod.length() > 0) videos.put(vod);
                start = itemEnd;
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
            String apiUrl = API_HOST + "/player/?url=" + id;
            String res = fetch(apiUrl);
            
            int urlStart = res.indexOf("\"url\":\"");
            if (urlStart != -1) {
                urlStart += 7;
                int urlEnd = res.indexOf("\"", urlStart);
                if (urlEnd != -1) {
                    JSONObject result = new JSONObject();
                    result.put("parse", 0);
                    result.put("url", res.substring(urlStart, urlEnd));
                    return result.toString();
                }
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "{\"parse\":1,\"url\":\"\"}";
    }
}
