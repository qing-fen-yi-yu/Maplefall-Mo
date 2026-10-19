package com.guilu.enums;

import com.guilu.DynamicThreadPool.FLDynamicReject.DynamicRejectHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.RejectedExecutionHandler;

@Getter
@AllArgsConstructor
public enum RejectHandlerType {
    CALLERRUNS("CALLERRUNS"),
    DISCARDOLDEST("DISCARDOLDEST"),
    DISCARD("DISCARD"),
    ABORT("ABORT"),
    DYNAMIC("DYNAMIC");
    private String rejectType;
    public static RejectedExecutionHandler of(String rejectType){
        if(rejectType.equals(CALLERRUNS.rejectType)){
            return new DynamicRejectHandler().getCallerRunsPolicy();
        } else if(rejectType.equals(DISCARDOLDEST.rejectType)){
            return  new DynamicRejectHandler().getCallerRunsPolicy();
        } else if (rejectType.equals(DISCARD.rejectType)){
            return new DynamicRejectHandler().getCallerRunsPolicy();
        } else if(rejectType.equals(ABORT.rejectType)){
            return  new DynamicRejectHandler().getCallerRunsPolicy();
        } else if(rejectType.equals(DYNAMIC.rejectType)){
            return new DynamicRejectHandler();
        }
        return  null;
    }

}
