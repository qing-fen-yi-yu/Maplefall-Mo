package com.guilu.domain;

import com.guilu.constants.ResultInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "通用响应结果")
public class Result<T> {
    @ApiModelProperty(value = "业务状态码，200-成功，其它-失败")
    private int code;
    @ApiModelProperty(value = "响应消息", example = "OK")
    private String message;
    @ApiModelProperty(value = "响应数据")
    private T data;
    @ApiModelProperty(value = "请求id", example = "1af123c11412e")
    private String requestId;

    public static <T> Result<T> success() {
        return new Result<>(ResultInfo.Code.SUCCESS);
    }
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultInfo.Code.SUCCESS,data);
    }
    public static <T> Result<T> success(Integer code, T data) {
        return new Result<>(code,data);
    }
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code,message);
    }
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultInfo.Code.FAILED,message);
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public Result(Integer code, T data) {
        this.code = code;
        this.data = data;
    }
    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public Result(Integer code) {
        this.code =  code;
    }

    public void code(int code) {
        this.code = code;
    }

    public void message(String message) {
        this.message = message;
    }

    public void data(T data) {
        this.data = data;
    }

    public void requestId(String requestId) {
        this.requestId = requestId;
    }
}
