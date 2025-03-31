package com.chain.result;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一的json结果的封装
 */
public class AjaxResult {
    public static final int SUCCESS = 0;
    public static final int FAIL = 1;
    public static final int INVALID_USER_INFO = 605;

    public static Map<String, Object> success() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", SUCCESS);
        result.put("msg", "成功");
        return result;
    }

    public static Map<String, Object> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", SUCCESS);
        result.put("msg", "成功");
        result.put("data", data);
        return result;
    }

    public static Map<String, Object> fail() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", FAIL);
        result.put("errmsg", "失败");
        return result;
    }

    public static Map<String, Object> fail(int code, String errmsg) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("errmsg", errmsg);
        return result;
    }
    public static Map<String, Object> fail(String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", FAIL);
        result.put("errmsg", msg);
        return result;
    }


}
