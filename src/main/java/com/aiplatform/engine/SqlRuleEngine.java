package com.aiplatform.engine;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SqlRuleEngine {

    public List<String> analyzeSql(String sql) {
        List<String> warnings = new ArrayList<>();
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                Select select = (Select) statement;
                if (select.getSelectBody() instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                    
                    // 1. 检查是否使用了 SELECT *
                    boolean hasSelectAll = false;
                    if (plainSelect.getSelectItems() != null) {
                        for (SelectItem item : plainSelect.getSelectItems()) {
                            if (item instanceof AllColumns || item instanceof AllTableColumns) {
                                hasSelectAll = true;
                                break;
                            }
                        }
                    }
                    if (hasSelectAll) {
                        warnings.add("检测到使用了 'SELECT *'，建议明确指定所需列以提高查询性能并减少网络传输。");
                    }

                    // 2. 检查是否缺失 WHERE 条件
                    if (plainSelect.getWhere() == null) {
                        warnings.add("检测到缺失 'WHERE' 条件，全表扫描可能导致严重的性能问题。");
                    }
                }
            }
        } catch (JSQLParserException e) {
            warnings.add("SQL 解析失败，请检查 SQL 语法是否正确: " + e.getMessage());
        }
        return warnings;
    }
}
