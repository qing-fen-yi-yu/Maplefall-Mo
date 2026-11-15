package com.guilu.Event;

import org.springframework.context.ApplicationEvent;

//线程池快照刷新事件
public class TPRefreshEvent extends ApplicationEvent {
    public TPRefreshEvent(Object source) {
        super(source);
    }
}
