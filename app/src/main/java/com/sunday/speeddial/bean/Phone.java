package com.sunday.speeddial.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2018/8/5.
 */

public class Phone extends DataSupport {

    /**
     * 主键
     */
    private int id;

    /**
     * 电话
     */
    private String phone;

    /**
     * 名称
     */
    private String name;

    /**
     * 照片
     */
    private String photo;

    /**
     * 排序
     */
    private int sort;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}
