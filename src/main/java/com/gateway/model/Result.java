package com.gateway.model;

public class Result {

    /**
     * 响应状态码
     */
    private int code;
    /**
     * 响应实体数据
     */
    private Object data;
    /**
     * 响应消息
     */
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
