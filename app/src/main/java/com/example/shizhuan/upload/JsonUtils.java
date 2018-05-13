package com.example.shizhuan.upload;

import net.sf.json.JSONObject;

/**
 * JSON工具方法类
 * Created by ShiZhuan on 2018/4/18.
 */

public class JsonUtils {
    /**
     * JSON-Lib 对象转JSON
     */
    public void entity2json(Object model) {

        //对象转JSON
        JSONObject json = JSONObject.fromObject(model);
    }
}
