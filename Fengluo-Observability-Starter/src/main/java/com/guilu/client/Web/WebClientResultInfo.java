package com.guilu.client.Web;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebClientResultInfo {
    private boolean success;
    private Object result;
    private String errorMsg;
}
