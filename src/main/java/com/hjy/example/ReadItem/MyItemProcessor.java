package com.hjy.example.ReadItem;

import org.springframework.batch.item.*;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MyItemProcessor<T> implements ItemProcessor<T, T> {

    private final Map<String, Object> key = new ConcurrentHashMap<>();
    private final Function<T, String> keyFunc;
    private final boolean allow;

    public MyItemProcessor(Function<T, String> keyFunc, boolean allow) {
        this.keyFunc = keyFunc;
        this.allow = allow;
    }

    @Override
    public T process(T item) throws Exception {
        if(allow){
            return item;
        }

        String checkKey = keyFunc.apply(item);

        if(key.containsKey(checkKey)){
            return null;
        }
        key.put(checkKey, checkKey);

        return item;
    }
}
