package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sql_optimize_record")
public class SqlOptimizeRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    
    private String userSql;
    
    private String dbType;
    
    private String aiModel;
    
    private Integer score;
    
    private String aiAnalysis;
    
    private Date createTime;
}
