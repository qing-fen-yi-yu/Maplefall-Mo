package com.guilu.monitor.sdk.binder.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;


@Configuration
public class CustomerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private String level;
    public CustomerAppender(){
    }
    @Override
    protected void append(ILoggingEvent loggingEvent) {
        Level level = loggingEvent.getLevel();
        String loggerName = loggingEvent.getLoggerName();
        String formattedMessage = loggingEvent.getFormattedMessage();
        long contextBirthTime = loggingEvent.getTimeStamp();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String time = simpleDateFormat.format(new Date(contextBirthTime));
    }
}
