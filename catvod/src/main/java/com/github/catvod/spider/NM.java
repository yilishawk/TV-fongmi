package com.github.catvod.spider;

import android.content.Context;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.net.OkHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 农民影视 - 纯Java实现，不依赖外部库
 * 站点: vip.wwgz.cn:5200
 */
public class NM extends Spider {

    private static final String siteUrl = "https://vip.wwgz.cn:5200";
    private static final String apiHost = "https://api.wwgz.cn:520";
    private static final String UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36";

    private Map<String, String> headers;

    @Override
    public void init(Context context) throws Exception {
        super.init(context);
        headers = new HashMap<>();
        headers.put("User-Agent", UA);
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        SpiderDebug.log("[NM] 初始化成功");
    }

    private String fetch(String url) throws Exception {
        return OkHttp.string(url, headers);
    }

    /**
     * 从HTML中提取指定标签的内容
     */
    private String getTagContent(String html, String tag, int index) {
        if (html == null || html.isEmpty()) return "";
        String openTag = "<" + tag + ">";
        String closeTag = "</" + tag + ">";
        int start = 0;
        for (int i = 0; i <= index; i++) {
            start = html.indexOf(openTag, start);
            if (start == -1) return "";
            start += openTag.length();
        }
        int end = html.indexOf(closeTag, start);
        if (end == -1) return "";
        return html.substring(start, end).trim();
    }

    /**
     * 从HTML中提取属性值
     */
    private String getAttr(String html, String attr, int index) {
        if (html == null || html.isEmpty()) return "";
        String pattern = attr + "=\"";
        int start = 0;
        for (int i = 0; i <= index; i++) {
            start = html.indexOf(pattern, start);
            if (start == -1) return "";
            start += pattern.length();
        }
        int end = html.indexOf("\"", start);
        if (end == -1) return "";
        return html.substring(start, end).trim();
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();

            String[][] classArr = {
                    {"12", "国产剧"},
                    {"1", "电影"},
                    {"2", "电视剧"},
                    {"3", "综艺"},
                    {"26", "短剧"}
            };
            for (String[] c : classArr) {
                JSONObject obj = new JSONObject();
                obj.put("type_id", c[0]);
                obj.put("type_name", c[1]);
                classes.put(obj);
            }
            result.put("class", classes);

            if (filter) {
                JSONObject filters = new JSONObject();

                JSONArray areaOptions = new JSONArray();
                areaOptions.put(createOption("全部", ""));
                for (String area : new String[]{"大陆","香港","台湾","美国","日本","韩国","英国","法国","泰国","新加坡","马来西亚","印度","加拿大","西班牙","俄罗斯","其它"}) {
                    areaOptions.put(createOption(area, area));
                }

                JSONArray yearOptions = new JSONArray();
                yearOptions.put(createOption("全部", "0"));
                for (int y = 2025; y >= 2005; y--) {
                    yearOptions.put(createOption(String.valueOf(y), String.valueOf(y)));
                }

                JSONArray orderOptions = new JSONArray();
                orderOptions.put(createOption("最新", "time"));
                orderOptions.put(createOption("最热", "hits"));
                orderOptions.put(createOption("评分", "score"));

                JSONArray movieType = new JSONArray();
                movieType.put(createOption("全部", "0"));
                String[][] mTypes = {{"动作片","5"},{"喜剧片","6"},{"爱情片","7"},{"科幻片","8"},{"恐怖片","9"},{"剧情片","10"},{"战争片","11"},{"惊悚片","16"},{"奇幻片","17"}};
                for (String[] t : mTypes) movieType.put(createOption(t[0], t[1]));

                JSONArray tvType = new JSONArray();
                tvType.put(createOption("全部", "0"));
                String[][] tvTypes = {{"国产剧","12"},{"港台泰","13"},{"日韩剧","14"},{"欧美剧","15"}};
                for (String[] t : tvTypes) tvType.put(createOption(t[0], t[1]));

                JSONArray onlyAll = new JSONArray();
                onlyAll.put(createOption("全部", "0"));

                JSONArray movieFilters = new JSONArray();
                movieFilters.put(createFilter("class", "类型", movieType));
                movieFilters.put(createFilter("area", "地区", areaOptions));
                movieFilters.put(createFilter("year", "年份", yearOptions));
                movieFilters.put(createFilter("order", "排序", orderOptions));
                filters.put("1", movieFilters);

                JSONArray domesticFilters = new JSONArray();
                domesticFilters.put(createFilter("area", "地区", areaOptions));
                domesticFilters.put(createFilter("year", "年份", yearOptions));
                domesticFilters.put(createFilter("order", "排序", orderOptions));
                filters.put("12", domesticFilters);

                JSONArray tvFilters = new JSONArray();
                tvFilters.put(createFilter("class", "类型", tvType));
                tvFilters.put(createFilter("area", "地区", areaOptions));
                tvFilters.put(createFilter("year", "年份", yearOptions));
                tvFilters.put(createFilter("order", "排序", orderOptions));
                filters.put("2", tvFilters);

                JSONArray varietyFilters = new JSONArray();
                varietyFilters.put(createFilter("class", "类型", onlyAll));
                varietyFilters.put(createFilter("area", "地区", areaOptions));
                varietyFilters.put(createFilter("year", "年份", yearOptions));
                varietyFilters.put(createFilter("order", "排序", orderOptions));
                filters.put("3", varietyFilters);

                JSONArray shortFilters = new JSONArray();
                shortFilters.put(createFilter("class", "类型", onlyAll));
                shortFilters.put(createFilter("area", "地区", areaOptions));
                shortFilters.put(createFilter("year", "年份", yearOptions));
                shortFilters.put(createFilter("order", "排序", orderOptions));
                filters.put("26", shortFilters);

                result.put("filters", filters);
            }

            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "{\"msg\":\"" + (e == null ? "未知错误" : e.getMessage()) + "\"}";
        }
    }

    private JSONObject createOption(String n, String v) throws Exception {
        JSONObject opt = new JSONObject();
        opt.put("n", n);
        opt.put("v", v);
        return opt;
    }

    private JSONObject createFilter(String key, String name, JSONArray value) throws Exception {
        JSONObject f = new JSONObject();
        f.put("key", key);
        f.put("name", name);
        f.put("value", value);
        return f;
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            if (extend == null) extend = new HashMap<>();
            String order = extend.getOrDefault("order", "time");
            String classId = extend.getOrDefault("class", "0");
            String year = extend.getOrDefault("year", "0");
            String area = extend.getOrDefault("area", "");

            String classParam = "0";
            String listId;
            if (!classId.equals("0")) {
                listId = classId;
            } else {
                listId = tid;
            }

            String yearPart = year.equals("0") ? "--" : "-" + year;
            String areaPart;
            if (area.isEmpty()) {
                areaPart = "--";
            } else {
                try {
                    areaPart = "-" + URLEncoder.encode(area, "UTF-8");
                } catch (Exception e) {
                    areaPart = "-" + area;
                }
            }

            String url = siteUrl + String.format(
                    "/vod-list-id-%s-pg-%s-order--by-%s-class-%s-year%s-letter--area%s-lang-.html",
                    listId, pg, order, classParam, yearPart, areaPart
            );

            String html = fetch(url);
            
            // 提取视频列表 - 简单HTML解析
            JSONArray videoList = new JSONArray();
            
            // 查找所有li标签
            int liStart = html.indexOf("<ul", html.indexOf("resize_list"));
            if (liStart == -1) {
                // 尝试其他容器
                liStart = html.indexOf("<ul");
            }
            if (liStart != -1) {
                int liEnd = html.indexOf("</ul>", liStart);
                if (liEnd != -1) {
                    String ulContent = html.substring(liStart, liEnd);
                    
                    // 分割每个li
                    String[] items = ulContent.split("<li");
                    for (String item : items) {
                        if (!item.contains("href=")) continue;
                        
                        JSONObject vod = new JSONObject();
                        
                        // 提取链接
                        int hrefStart = item.indexOf("href=\"");
                        if (hrefStart != -1) {
                            hrefStart += 6;
                            int hrefEnd = item.indexOf("\"", hrefStart);
                            if (hrefEnd != -1) {
                                String href = item.substring(hrefStart, hrefEnd);
                                vod.put("vod_id", href.contains("/vod-detail-id-") ? "detail_" + href.split("-")[3].replace(".html", "") : href);
                            }
                        }
                        
                        // 提取标题
                        int titleStart = item.indexOf(">");
                        if (titleStart != -1) {
                            int titleEnd = item.indexOf("</", titleStart);
                            if (titleEnd != -1) {
                                String title = item.substring(titleStart + 1, titleEnd).trim();
                                if (!title.isEmpty()) {
                                    vod.put("vod_name", title);
                                }
                            }
                        }
                        
                        // 提取图片
                        int imgStart = item.indexOf("data-echo=\"");
                        if (imgStart != -1) {
                            imgStart += 11;
                            int imgEnd = item.indexOf("\"", imgStart);
                            if (imgEnd != -1) {
                                vod.put("vod_pic", item.substring(imgStart, imgEnd));
                            }
                        }
                        
                        // 提取备注
                        if (vod.length() > 0) {
                            videoList.put(vod);
                        }
                    }
                }
            }

            JSONObject result = new JSONObject();
            result.put("list", videoList);
            result.put("pagecount", 1);
            result.put("page", Integer.parseInt(pg));
            result.put("limit", videoList.length());
            result.put("total", videoList.length());
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "{\"list\":[],\"pagecount\":1,\"page\":1,\"limit\":0,\"total\":0}";
        }
    }

    @Override
    public String detailContent(List<String> ids) {
        try {
            String vodId = ids.get(0);
            String detailId = "";
            String detailUrl;
            if (vodId.startsWith("detail_")) {
                detailId = vodId.substring(7);
                detailUrl = siteUrl + "/vod-detail-id-" + detailId + ".html";
            } else {
                detailUrl = vodId.startsWith("http") ? vodId : siteUrl + vodId;
                // 提取详情ID
                int idStart = detailUrl.indexOf("vod-detail-id-");
                if (idStart != -1) {
                    idStart += "vod-detail-id-".length();
                    int idEnd = detailUrl.indexOf(".html", idStart);
                    if (idEnd != -1) {
                        detailId = detailUrl.substring(idStart, idEnd);
                    }
                }
            }

            String html = fetch(detailUrl);
            
            JSONObject vod = new JSONObject();
            vod.put("vod_id", vodId);
            
            // 提取标题
            int titleStart = html.indexOf("<h1");
            if (titleStart != -1) {
                int titleEnd = html.indexOf("</h1>", titleStart);
                if (titleEnd != -1) {
                    String title = html.substring(titleStart, titleEnd).replaceAll("<[^>]+>", "").trim();
                    vod.put("vod_name", title);
                }
            }
            
            // 提取图片
            int picStart = html.indexOf("data-echo=\"");
            if (picStart == -1) picStart = html.indexOf("src=\"");
            if (picStart != -1) {
                picStart += 9;
                int picEnd = html.indexOf("\"", picStart);
                if (picEnd != -1) {
                    vod.put("vod_pic", html.substring(picStart, picEnd));
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
            String pg = "1";
            String url = siteUrl + "/vod-search-pg-" + pg + "-wd-" + URLEncoder.encode(key, "UTF-8") + ".html";
            String html = fetch(url);
            
            JSONArray videoList = new JSONArray();
            
            // 简单解析搜索结果
            String[] items = html.split("<li");
            for (String item : items) {
                if (!item.contains("href=")) continue;
                
                JSONObject v = new JSONObject();
                
                // 提取链接
                int hrefStart = item.indexOf("href=\"");
                if (hrefStart != -1) {
                    hrefStart += 6;
                    int hrefEnd = item.indexOf("\"", hrefStart);
                    if (hrefEnd != -1) {
                        String href = item.substring(hrefStart, hrefEnd);
                        if (href.contains("/vod-detail-id-")) {
                            String detailId = href.split("-")[3].replace(".html", "");
                            v.put("vod_id", "detail_" + detailId);
                        } else {
                            v.put("vod_id", href);
                        }
                    }
                }
                
                // 提取标题
                int titleStart = item.indexOf(">");
                if (titleStart != -1) {
                    int titleEnd = item.indexOf("</", titleStart);
                    if (titleEnd != -1) {
                        String title = item.substring(titleStart + 1, titleEnd).trim();
                        if (!title.isEmpty()) {
                            v.put("vod_name", title);
                        }
                    }
                }
                
                if (v.length() > 0) {
                    videoList.put(v);
                }
            }

            JSONObject result = new JSONObject();
            result.put("list", videoList);
            result.put("page", Integer.parseInt(pg));
            result.put("pagecount", 1);
            result.put("limit", videoList.length());
            result.put("total", videoList.length());
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "{\"list\":[],\"page\":1,\"pagecount\":1,\"limit\":0,\"total\":0}";
        }
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            if (id != null && !id.contains("http") && !id.contains("$") && !id.contains("?")) {
                String apiUrl = apiHost + "/player/?url=" + id;
                String res = fetch(apiUrl);
                
                // 提取URL
                int urlStart = res.indexOf("\"url\":");
                if (urlStart != -1) {
                    urlStart = res.indexOf("\"", urlStart + 6);
                    int urlEnd = res.indexOf("\"", urlStart + 1);
                    if (urlEnd != -1) {
                        String realUrl = res.substring(urlStart + 1, urlEnd).replace("\\u0026", "&");
                        JSONObject result = new JSONObject();
                        result.put("parse", 0);
                        result.put("url", realUrl);
                        JSONObject header = new JSONObject();
                        header.put("User-Agent", UA);
                        result.put("header", header);
                        return result.toString();
                    }
                }
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "{\"parse\":1,\"url\":\"\"}";
    }
}
