package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("video_generate_record")
public class VideoGenerateRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userPrompt;

    private String aiModel;

    private String taskId;

    private String taskStatus;

    private String videoUrl;

    private String rawResponse;

    private Date createTime;
}
