package com.aiplatform.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("marketing_generate_record")
public class MarketingGenerateRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String recordType;

    private String productName;

    private String targets;

    private String aiModel;

    private String inputPayload;

    private String resultPayload;

    private String status;

    private Date createTime;
}
