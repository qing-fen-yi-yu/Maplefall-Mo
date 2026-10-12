package com.guilu.service;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.core.lang.Snowflake;
import com.guilu.constants.ExceptionConstants;
import com.guilu.constants.JwtConstants;
import com.guilu.domain.Enum.CodeTypeEnum;
import com.guilu.domain.vo.ImageCodeVO;
import com.guilu.exception.BusinessException.BadDataException;
import com.guilu.exception.BusinessException.CommonException;
import com.guilu.exception.RequestException.BadRequestException;
import com.guilu.utils.BooleanUtils;
import com.guilu.utils.CaptchaUtils;
import com.guilu.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ImageCodeService {
    private final StringRedisTemplate redisTemplate;
    public boolean verifyCode(String code,String Id) {
        if(code == null || Id == null || code.isBlank() || Id.isBlank()){
            return false;
        }
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String vail = operations.getAndDelete(JwtConstants.IMAGE_CODE_KEY + Id);
        if(vail==null){
            throw new BadDataException(ExceptionConstants.MESSAGE.CACHEDATAEXCEPTION);
        }
        return BooleanUtils.isTrue(vail.equals(code));
    }
    private Long saveCode(String code){
        Long id = new Snowflake().nextId();
        // 返回验证码 ID 前完成保存，避免立即登录时验证码还未写入；五分钟后过期。
        redisTemplate.opsForValue().set(JwtConstants.IMAGE_CODE_KEY+id,code, Duration.ofMinutes(5));
        if(ObjectUtils.hasNull(id)){
            throw new BadDataException();
        }
        return id;
    }
    public ImageCodeVO createImageCode(CodeTypeEnum codeType) {
        AbstractCaptcha captcha = null;
        switch (codeType){
            case LINECAPTCHA -> {
                captcha = CaptchaUtils.getLine();
            }
            case SHEARCAPTCHA -> {
                captcha = CaptchaUtils.getShare();
            }
            case CIRCLECAPTCHA -> {
                captcha = CaptchaUtils.getCircle();
            }
        }
        ImageCodeVO imageCodeVO = new ImageCodeVO();
        try {
            if (ObjectUtils.hasNull(captcha)) {
                throw new BadRequestException("验证码生成失败");
            }
            imageCodeVO.setImage("data:image/png;base64," + captcha.getImageBase64());
            String code = captcha.getCode();
            Long id = saveCode(code);
            imageCodeVO.setId(id);
        } catch (Exception e) {
            throw new BadRequestException("验证码生成失败");
        }
        return imageCodeVO;
    }
}
