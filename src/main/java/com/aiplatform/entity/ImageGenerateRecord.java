package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("image_generate_record")
public class ImageGenerateRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userPrompt;

    private String aiModel;

    private String imageUrls;

    private String rawResponse;
    
    private String status;

    private Date createTime;
}
