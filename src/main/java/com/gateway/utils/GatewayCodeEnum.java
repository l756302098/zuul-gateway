package com.gateway.utils;

public enum GatewayCodeEnum {

    //成功
    OK(200),
    //token 为空
    TOKEN_NULL(450),
    //token 无效
    TOKEN_INVALID(451),
    //token 过期
    TOKEN_EXPIRE(452),
    //40010
    SIGN_NOT_VALID(470),
    //IP 无效
    IP_FORBIDDEN(460);


    int type;

    GatewayCodeEnum(int type){
        this.type = type;
    }

    public int getValue()
    {
        return type;
    }

    public String getInfo(){
        String info = "";
        switch (type){
            case 200:info="成功";break;
            case 450:info="token 为空";break;
            case 451:info="token 无效";break;
            case 452:info="token 过期";break;
            case 460:info="Ip is forbidden";break;
            case 470:info="签名校验失败";break;
            default:info="服务器错误!";break;
        }

        return info;

    }

    /**
     * 按照Value获得枚举值
     */
    public static GatewayCodeEnum valueOf(Integer value) {
        if (value != null) {
            for (GatewayCodeEnum fsEnum : GatewayCodeEnum.values()) {
                if (fsEnum.getValue() == value) {
                    return fsEnum;
                }
            }
        }
        return null;
    }


}
