package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("camera_generate_record")
public class CameraGenerateRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String originalImageUrl;

    private String aiModel;

    private Integer azimuth;

    private Integer elevation;

    private Double distance;

    private String promptUsed;

    private String generatedImageUrl;

    private String rawResponse;
    
    private String status;

    private Date createTime;
}
