package com.example.assetperform.common.result;

import lombok.Data;

@Data 
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp = System.currentTimeMillis();


    // 带参数的success
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);

        return result;
    }

    //不带参数的success
    public static <T> Result<T> success(){

        return success(null);
    }

    // 失败，带一个code参数
    public static <T> Result<T> error(int code, String message){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);

        return result;
    }    
}
