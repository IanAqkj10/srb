package com.ian.srb.common.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author:IanJ
 * @date:2021/6/14 16:10
 */
@Data
public class Rs {

    private Integer code;
    private String message;
    private Map<String, Object> data = new HashMap<>();


    private Rs() {
    }

    ;

    public static Rs success() {
        Rs rs = new Rs();
        rs.setCode(ResponseEnum.SUCCESS.getCode());
        rs.setMessage(ResponseEnum.SUCCESS.getMessage());
        return rs;
    }

    public static Rs error() {
        Rs rs = new Rs();
        rs.setCode(ResponseEnum.ERROR.getCode());
        rs.setMessage(ResponseEnum.ERROR.getMessage());
        return rs;
    }

    public static Rs myrs(ResponseEnum responseEnum) {
        Rs rs = new Rs();
        rs.setMessage(responseEnum.getMessage());
        rs.setCode(responseEnum.getCode());
        return rs;
    }

    public Rs data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public Rs message(String message) {
        this.setMessage(message);
        return this;
    }

    public Rs code(Integer code) {
        this.setCode(code);
        return this;
    }

}
