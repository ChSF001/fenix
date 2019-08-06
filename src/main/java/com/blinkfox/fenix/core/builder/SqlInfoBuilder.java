package com.blinkfox.fenix.core.builder;

import com.blinkfox.fenix.bean.BuildSource;
import com.blinkfox.fenix.bean.SqlInfo;
import com.blinkfox.fenix.consts.Const;
import com.blinkfox.fenix.consts.LikeTypeEnum;
import com.blinkfox.fenix.consts.SymbolConst;
import com.blinkfox.fenix.helper.StringHelper;

import java.util.Map;

/**
 * 构建拼接 JPQL 或者 SQL 语句片段和参数的构建器类.
 *
 * @author blinkfox on 2019-08-06.
 * @see XmlSqlInfoBuilder
 */
public class SqlInfoBuilder {

    /**
     * {@link SqlInfo} 对象.
     */
    private SqlInfo sqlInfo;

    /**
     * 上下文参数（一般是 Bean 或者 map）.
     */
    Object context;

    /**
     * 生成的 SQL 片段的前缀.
     */
    private String prefix;

    /**
     * SQL 标记操作符.
     */
    private String symbol;

    /**
     * 其它数据.
     *
     * <p>注：通常情况下这个值是 NULL，如果某些情况下，你需要传递额外的参数值，可以通过这个属性来传递，
     *      是为了方便传递或处理数据而设计的.</p>
     */
    private Map<String, Object> others;

    /**
     * 私有构造方法.
     *
     * @param source 构建资源参数
     */
    SqlInfoBuilder(BuildSource source) {
        this.sqlInfo = source.getSqlInfo();
        this.context = source.getContext();
        this.prefix = source.getPrefix();
        this.symbol = source.getSymbol();
        this.others = source.getOthers();
    }

    /**
     * 为了生成 JPQL 语法中 " :text " 这种冒号式的命名参数，需要将 "." 点号替换为 "_"，Spring DATA JPA 才支持.
     *
     * <p>如：JPQL 语句片段 " b.title = :blog.title "，会将 "blog.title" 替换为 "blog_title".</p>
     *
     * @param text 待替换的文本
     * @return 替换后的文本
     */
    private String fixDot(String text) {
        return text.contains(Const.DOT) ? text.replace(Const.DOT, Const.UNDERLINE) : text;
    }

    /**
     * 构建常规 SQL 片段的 {@link SqlInfo} 信息.
     * <p>如：'u.id = :id'.</p>
     *
     * @param fieldText JPQL 或者 SQL 语句的字段的文本.
     * @param valueText 待解析 value 的文本值
     * @param value 解析后的表达式的值
     */
    protected void buildNormalSql(String fieldText, String valueText, Object value) {
        String namedText = this.fixDot(valueText);
        sqlInfo.getJoin().append(this.prefix).append(fieldText)
                .append(this.symbol).append(Const.COLON).append(namedText);
        sqlInfo.getParams().put(namedText, value);
    }

    /**
     * 构建 LIKE 模糊查询 SQL 片段的 {@link SqlInfo} 信息.
     * <p>如：'u.id LIKE :id'.</p>
     *
     * @param fieldText 数据库字段的文本
     * @param valueText 待解析 value 的文本值
     * @param value 参数值
     */
    protected void buildLikeSql(String fieldText, String valueText, Object value) {
        String namedText = this.fixDot(valueText);
        sqlInfo.getJoin().append(this.prefix).append(fieldText)
                .append(StringHelper.isBlank(this.symbol) ? SymbolConst.LIKE : this.symbol)
                .append(Const.COLON).append(namedText);

        // 如果 others 参数为空，说明是前后模糊的情况.
        if (this.others == null || this.others.size() == 0) {
            sqlInfo.getParams().put(namedText, "%" + value + "%");
            return;
        }

        // 如果 others 参数不为空，获取对应的类型设置参数.
        LikeTypeEnum likeTypeEnum = (LikeTypeEnum) this.others.get(Const.TYPE);
        if (likeTypeEnum == LikeTypeEnum.STARTS_WITH) {
            sqlInfo.getParams().put(namedText, value + "%");
        } else if (likeTypeEnum == LikeTypeEnum.ENDS_WITH) {
            sqlInfo.getParams().put(namedText, "%" + value);
        }
    }

    /**
     * 根据指定的模式 `pattern` 来追加构建 LIKE 模糊查询 SQL 片段的 {@link SqlInfo} 信息.
     *
     * @param fieldText 数据库字段的文本
     * @param pattern LIKE 匹配的模式
     */
    protected void buildLikePatternSql(String fieldText, String pattern) {
        sqlInfo.getJoin().append(prefix).append(fieldText)
                .append(StringHelper.isBlank(this.symbol) ? SymbolConst.LIKE : this.symbol)
                .append(Const.QUOTE).append(pattern).append(Const.QUOTE);
    }

    /**
     * 构建区间查询的SqlInfo信息.
     * @param fieldText 数据库字段文本
     * @param startValue 参数开始值
     * @param endValue 参数结束值
     * @return 返回SqlInfo信息
     */
    public void buildBetweenSql(String fieldText, Object startValue, Object endValue) {
        /* 根据开始文本和结束文本判断执行是大于、小于还是区间的查询sql和参数的生成 */
//        if (startValue != null && endValue == null) {
//            join.append(prefix).append(fieldText).append(Const.GTE_SUFFIX);
//            params.add(startValue);
//        } else if (startValue == null && endValue != null) {
//            join.append(prefix).append(fieldText).append(Const.LTE_SUFFIX);
//            params.add(endValue);
//        } else {
//            join.append(prefix).append(fieldText).append(Const.BT_AND_SUFFIX);
//            params.add(startValue);
//            params.add(endValue);
//        }
    }

    /**
     * 构建" IN "范围查询的SqlInfo信息.
     * @param fieldText 数据库字段文本
     * @param values 对象数组的值
     * @return 返回SqlInfo信息
     */
    public void buildInSql(String fieldText, Object[] values) {
//        if (values == null || values.length == 0) {
//            return sqlInfo;
//        }
//
//        // 遍历数组，并遍历添加in查询的替换符和参数
//        this.symbol = StringHelper.isBlank(this.symbol) ? Const.IN_SUFFIX : this.symbol;
//        join.append(prefix).append(fieldText).append(this.symbol).append("(");
//        int len = values.length;
//        for (int i = 0; i < len; i++) {
//            if (i == len - 1) {
//                join.append("?) ");
//            } else {
//                join.append("?, ");
//            }
//            params.add(values[i]);
//        }
    }

    /**
     * 构建" IS NULL "和" IS NOT NULL "需要的SqlInfo信息.
     * @param fieldText 数据库字段的文本
     * @return SqlInfo信息
     */
    public void buildIsNullSql(String fieldText) {
//        this.suffix = StringHelper.isBlank(this.suffix) ? Const.IS_NULL_SUFFIX : this.suffix;
//        join.append(prefix).append(fieldText).append(this.suffix);
    }

}