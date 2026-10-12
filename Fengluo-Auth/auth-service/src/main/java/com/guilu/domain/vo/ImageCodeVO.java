package com.guilu.domain.vo;

import lombok.Data;

import java.awt.*;
@Data
public class ImageCodeVO {
    /** PNG data URL，可直接用于浏览器 img.src，避免序列化 AWT 内部对象。 */
    private String image;
    private Long id;
}
