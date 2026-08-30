package com.github.catvod.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Result {
    @SerializedName("class")
    private List<Class> classes;
    @SerializedName("list")
    private List<Vod> list;
    @SerializedName("url")
    private String url;
    @SerializedName("parse")
    private int parse;
    @SerializedName("page")
    private Integer page;
    @SerializedName("pagecount")
    private Integer pagecount;
    @SerializedName("limit")
    private Integer limit;
    @SerializedName("total")
    private Integer total;
    @SerializedName("msg")
    private String msg;

    public static Result get() {
        return new Result();
    }

    public Result classes(List<Class> classes) {
        this.classes = classes;
        return this;
    }

    public Result vod(List<Vod> list) {
        this.list = list;
        return this;
    }

    public Result vod(Vod item) {
        this.list = java.util.Collections.singletonList(item);
        return this;
    }

    public Result url(String url) {
        this.url = url;
        return this;
    }

    public Result parse(int parse) {
        this.parse = parse;
        return this;
    }

    public Result page(int page, int count, int limit, int total) {
        this.page = page;
        this.pagecount = count;
        this.limit = limit;
        this.total = total;
        return this;
    }

    public Result msg(String msg) {
        this.msg = msg;
        return this;
    }

    public String string() {
        return new Gson().newBuilder().disableHtmlEscaping().create().toJson(this);
    }
}