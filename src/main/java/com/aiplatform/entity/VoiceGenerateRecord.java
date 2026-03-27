package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("voice_generate_record")
public class VoiceGenerateRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userText;

    private String aiModel;

    private String audioUrl;

    private String rawResponse;

    private Date createTime;
}
