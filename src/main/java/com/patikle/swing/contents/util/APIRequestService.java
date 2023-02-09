package com.patikle.swing.contents.util;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.util.MultiValueMap;

public interface APIRequestService {
    public <T> T get(String url,  Class<T> valueType);
    public <T> T get(String url, Map<String, String> headersMap, MultiValueMap<String, String> params, Class<T> valueType);
    public <T> T get(String url,  Class<T> valueType, String apikey, String secretkey);

    public <T> T post(String url,  Class<T> valueType);
    public <T> T post(String url, Map<String, String> headersMap, JSONObject jsonObject, Class<T> valueType);
    public <T> T post(String url,  Class<T> valueType, String apikey, String secretkey);

}
