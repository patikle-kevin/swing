package com.patikle.swing.contents.util;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class APIRequestServiceImpl implements APIRequestService {

    Logger logger = LoggerFactory.getLogger(APIRequestService.class);

    public <T> T get(String url, Map<String, String> headersMap, MultiValueMap<String, String> params,  Class<T> valueType){
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder() .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)).build(); 
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(url);
        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
        .defaultHeaders(httpHeaders -> {
            Iterator<String> iterator =  headersMap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                httpHeaders.set(key, headersMap.get(key));
            }
        })
        .exchangeStrategies(exchangeStrategies)
        .build();

        // MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // params.add("CANO", "50074422");
        // params.add("ACNT_PRDT_CD", "01");
        // params.add("OVRS_EXCG_CD", "NASD");
        // params.add("TR_CRCY_CD", "USD");
        // params.add("CTX_AREA_FK200", "");
        // params.add("CTX_AREA_NK200", "");

        Mono<String> body = webClient.get()
                .uri(uriBuilder -> uriBuilder.queryParams(params).build())
                .retrieve().bodyToMono(String.class);
        ;
        T t = null;
        try{
           t = new ObjectMapper().readValue(body.block(), valueType);
        }catch(JsonProcessingException e){
            
        }catch(Exception e){
            try{
                Thread.sleep(5000);
                body = webClient.get().retrieve().bodyToMono(String.class);
                try{
                    t = new ObjectMapper().readValue(body.block(), valueType);
                }catch(JsonProcessingException e2){
                }
            }catch(InterruptedException ex){
                
            }
        }
        return t;
    }
    
    public <T> T get(String url,  Class<T> valueType){
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder() .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)).build(); 
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(url);
        WebClient webClient = WebClient.builder()
        .uriBuilderFactory(factory)
        .exchangeStrategies(exchangeStrategies)
        .build();
        Mono<String> body = webClient.get().retrieve().bodyToMono(String.class);
        T t = null;
        try{
           t = new ObjectMapper().readValue(body.block(), valueType);
        }catch(JsonProcessingException e){
            
        }catch(Exception e){
            try{
                Thread.sleep(5000);
                body = webClient.get().retrieve().bodyToMono(String.class);
                try{
                    t = new ObjectMapper().readValue(body.block(), valueType);
                }catch(JsonProcessingException e2){
                }
            }catch(InterruptedException ex){
                
            }
        }
        return t;
    }

    public <T> T get(String url,  Class<T> valueType, String apikey, String secretkey){
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder() .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)).build(); 
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(url);
        WebClient webClient = WebClient.builder()
        .uriBuilderFactory(factory)
        .defaultHeaders(httpHeaders -> {
            httpHeaders.set("APCA-API-KEY-ID", apikey);
            httpHeaders.set("APCA-API-SECRET-KEY", secretkey);
        })
        .exchangeStrategies(exchangeStrategies)
        .build();
        Mono<String> body = webClient.get().retrieve().bodyToMono(String.class);
        T t = null;
        try{
           t = new ObjectMapper().readValue(body.block(), valueType);
        }catch(JsonProcessingException e){
        }catch(Exception e){
            try{
                Thread.sleep(5000);
                body = webClient.get().retrieve().bodyToMono(String.class);
                try{
                    t = new ObjectMapper().readValue(body.block(), valueType);
                }catch(JsonProcessingException e2){
                }
            }catch(InterruptedException ex){
                
            }
        }
        return t;
    }

    public <T> T post(String url,  Class<T> valueType){
        T t = null;
        return t;
    }
    public <T> T post(String url, Map<String, String> headersMap, JSONObject jsonObject, Class<T> valueType){
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder() .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)).build(); 
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(url);
        
        
        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
        .defaultHeaders(httpHeaders -> {
            Iterator<String> iterator =  headersMap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                httpHeaders.set(key, headersMap.get(key));
            }
        })
        .exchangeStrategies(exchangeStrategies)
        .build();

        // Mono<String> body = webClient.post()
        //         .uri(uriBuilder -> uriBuilder.queryParams(params).build())
        //         .retrieve().bodyToMono(String.class);
        Mono<String> body = webClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        // .body(BodyInserters.fromFormData(params))// fromValue(jsonString))
        .bodyValue(jsonObject)
        .retrieve().bodyToMono(String.class);
        ;
        T t = null;
        try{
           t = new ObjectMapper().readValue(body.block(), valueType);
        }catch(JsonProcessingException e){
            
        }catch(Exception e){
            try{
                Thread.sleep(5000);
                body = webClient.get().retrieve().bodyToMono(String.class);
                try{
                    t = new ObjectMapper().readValue(body.block(), valueType);
                }catch(JsonProcessingException e2){
                }
            }catch(InterruptedException ex){
                
            }
        }
        return t;
    }
    public <T> T post(String url,  Class<T> valueType, String apikey, String secretkey){
        T t = null;
        return t;
    }

}
