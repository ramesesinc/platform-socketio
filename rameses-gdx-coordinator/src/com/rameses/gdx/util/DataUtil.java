/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.gdx.util;

import com.rameses.osiris3.server.JsonUtil;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author jzamora
 */
public class DataUtil {
    
    public static Object getData(Object[] args) {
        return getData(args, null);
    }
    
    public static Object getData(Object[] args, String key) {
        if (args.length == 0) {
            return null;
        }

        if (args[0] instanceof JSONObject) {
            try {
                JSONObject json = (JSONObject) args[0];
                String str = json.toString();
                Object data = JsonUtil.toObject(str);
                if (data instanceof Map && key != null) {
                    Map map = (Map) data;
                    return map.get(key);
                }
                return data;
            } catch (Exception ex) {
                System.out.println("Error extracting value for " + key);
                return null;
            }
        } else {
            return args[0];
        }
    }
}
