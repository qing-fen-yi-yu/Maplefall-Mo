package com.guilu.utils;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;

import java.awt.*;

public class CaptchaUtils {
    private static Image getImage(AbstractCaptcha captcha){
        return captcha.getImage();
    }
    public static Image createLine() {
        return getImage(CaptchaUtil.createLineCaptcha(100,200));
    }
    public static Image createShare(){
        return getImage(CaptchaUtil.createShearCaptcha(100,200));
    }
    public static Image createCircle(){
        return getImage(CaptchaUtil.createCircleCaptcha(100,200));
    }
    public static Image createLine(int width,int height) {
        return getImage(CaptchaUtil.createLineCaptcha(width,height));
    }
    public static Image createShare(int width,int height){
        return getImage(CaptchaUtil.createShearCaptcha(width,height));
    }
    public static Image createCircle(int width,int height){
        return getImage(CaptchaUtil.createCircleCaptcha(width,height));
    }
    public static AbstractCaptcha getLine() {
        return CaptchaUtil.createLineCaptcha(100,200);
    }
    public static AbstractCaptcha getShare(){
        return CaptchaUtil.createShearCaptcha(100,200);
    }
    public static AbstractCaptcha getCircle(){
        return CaptchaUtil.createCircleCaptcha(100,200);
    }
}
