package com.zgg.core.练习.需求3;

import java.io.Serializable;

public class UserVisitAction implements Serializable {
    private String date;   //用户点击行为的日期
    private Integer user_id;   //用户的ID
    private String session_id;  //Session的ID
    private Integer page_id;  // 某个页面的ID
    private String action_time;  //动作的时间点
    private String search_keyword; //用户搜索的关键词
    private Integer click_category_id; //某一个商品品类的ID
    private Integer click_product_id; //某一个商品的ID
    private String order_category_ids; //一次订单中所有品类的ID集合
    private String order_product_ids; //一次订单中所有商品的ID集合
    private String pay_category_ids; //一次支付中所有品类的ID集合
    private String pay_product_ids; //一次支付中所有商品的ID集合
    private Integer city_id;  //城市 id

    public UserVisitAction() {
    }

    public UserVisitAction(String date, Integer user_id, String session_id, Integer page_id, String action_time, String search_keyword, Integer click_category_id, Integer click_product_id, String order_category_ids, String order_product_ids, String pay_category_ids, String pay_product_ids, Integer city_id) {
        this.date = date;
        this.user_id = user_id;
        this.session_id = session_id;
        this.page_id = page_id;
        this.action_time = action_time;
        this.search_keyword = search_keyword;
        this.click_category_id = click_category_id;
        this.click_product_id = click_product_id;
        this.order_category_ids = order_category_ids;
        this.order_product_ids = order_product_ids;
        this.pay_category_ids = pay_category_ids;
        this.pay_product_ids = pay_product_ids;
        this.city_id = city_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public Integer getPage_id() {
        return page_id;
    }

    public void setPage_id(Integer page_id) {
        this.page_id = page_id;
    }

    public String getAction_time() {
        return action_time;
    }

    public void setAction_time(String action_time) {
        this.action_time = action_time;
    }

    public String getSearch_keyword() {
        return search_keyword;
    }

    public void setSearch_keyword(String search_keyword) {
        this.search_keyword = search_keyword;
    }

    public Integer getClick_category_id() {
        return click_category_id;
    }

    public void setClick_category_id(Integer click_category_id) {
        this.click_category_id = click_category_id;
    }

    public Integer getClick_product_id() {
        return click_product_id;
    }

    public void setClick_product_id(Integer click_product_id) {
        this.click_product_id = click_product_id;
    }

    public String getOrder_category_ids() {
        return order_category_ids;
    }

    public void setOrder_category_ids(String order_category_ids) {
        this.order_category_ids = order_category_ids;
    }

    public String getOrder_product_ids() {
        return order_product_ids;
    }

    public void setOrder_product_ids(String order_product_ids) {
        this.order_product_ids = order_product_ids;
    }

    public String getPay_category_ids() {
        return pay_category_ids;
    }

    public void setPay_category_ids(String pay_category_ids) {
        this.pay_category_ids = pay_category_ids;
    }

    public String getPay_product_ids() {
        return pay_product_ids;
    }

    public void setPay_product_ids(String pay_product_ids) {
        this.pay_product_ids = pay_product_ids;
    }

    public Integer getCity_id() {
        return city_id;
    }

    public void setCity_id(Integer city_id) {
        this.city_id = city_id;
    }

    @Override
    public String toString() {
        return "UserVisitAction{" +
                "date='" + date + '\'' +
                ", user_id=" + user_id +
                ", session_id='" + session_id + '\'' +
                ", page_id=" + page_id +
                ", action_time='" + action_time + '\'' +
                ", search_keyword='" + search_keyword + '\'' +
                ", click_category_id=" + click_category_id +
                ", click_product_id=" + click_product_id +
                ", order_category_ids='" + order_category_ids + '\'' +
                ", order_product_ids='" + order_product_ids + '\'' +
                ", pay_category_ids='" + pay_category_ids + '\'' +
                ", pay_product_ids='" + pay_product_ids + '\'' +
                ", city_id=" + city_id +
                '}';
    }
}
