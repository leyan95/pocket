package org.hv.pocket.criteria;

import org.hv.pocket.annotation.Join;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.constant.AnnotationType;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.constant.SqlOperateTypes;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wujianchuan
 */
public class SqlBody {

    private Class<? extends AbstractEntity> clazz;
    private final List<Modern> modernList;
    private final List<Restrictions> restrictionsList;
    private final List<Sort> orderList;

    private SqlBody(Class<? extends AbstractEntity> clazz, List<Restrictions> restrictionsList, List<Modern> modernList, List<Sort> orderList) {
        this.clazz = clazz;
        this.restrictionsList = restrictionsList;
        this.modernList = modernList;
        this.orderList = orderList;
    }

    private SqlBody(List<Restrictions> restrictionsList, List<Modern> modernList, List<Sort> orderList) {
        this.restrictionsList = restrictionsList;
        this.modernList = modernList;
        this.orderList = orderList;
    }

    public static SqlBody newInstance(Class<? extends AbstractEntity> clazz, List<Restrictions> restrictions, List<Modern> modernList, List<Sort> orderList) {
        return new SqlBody(clazz, restrictions, modernList, orderList);
    }

    public static SqlBody newInstance(List<Restrictions> restrictions, List<Modern> modernList, List<Sort> orderList) {
        return new SqlBody(restrictions, modernList, orderList);
    }

    String buildSelectSql(DatabaseNodeConfig databaseConfig) {
        return CommonSql.SELECT +
                this.parseColumnSql() +
                CommonSql.FROM +
                MapperFactory.getTableName(clazz.getName()) +
                this.parseJoinSql() +
                this.parseRestrictionsSql(databaseConfig) +
                this.parseOrderSql();
    }

    String buildUpdateSql(List<ParameterTranslator> parameters, Map<String, Object> parameterMap, DatabaseNodeConfig databaseConfig) {
        return CommonSql.UPDATE +
                MapperFactory.getTableName(clazz.getName()) +
                CommonSql.SET +
                this.parserModernSql(parameters, parameterMap) +
                this.parseRestrictionsSql(databaseConfig);

    }

    String buildCountSql(DatabaseNodeConfig databaseConfig) {
        return CommonSql.SELECT +
                SqlOperateTypes.COUNT +
                "(0)" +
                CommonSql.FROM +
                MapperFactory.getTableName(clazz.getName()) +
                this.parseJoinSql() +
                this.parseRestrictionsSql(databaseConfig);
    }

    String buildDeleteSql(DatabaseNodeConfig databaseConfig) {
        return CommonSql.DELETE +
                CommonSql.FROM +
                MapperFactory.getTableName(clazz.getName()) +
                this.parseRestrictionsSql(databaseConfig);
    }

    String buildMaxSql(DatabaseNodeConfig databaseConfig, String fieldName) {
        String columnName;
        if (AnnotationType.JOIN.equals(MapperFactory.getAnnotationType(clazz.getName(), fieldName))) {
            Join join = (Join) MapperFactory.getAnnotation(clazz.getName(), fieldName);
            columnName = join.joinTableSurname() + CommonSql.DOT + join.destinationColumn();
        } else {
            columnName = MapperFactory.getTableName(this.clazz.getName()) + CommonSql.DOT + MapperFactory.getRepositoryColumnName(this.clazz.getName(), fieldName);
        }
        return CommonSql.SELECT +
                SqlOperateTypes.MAX +
                CommonSql.LEFT_BRACKET + columnName + CommonSql.RIGHT_BRACKET +
                CommonSql.FROM +
                MapperFactory.getTableName(clazz.getName()) +
                this.parseJoinSql() +
                this.parseRestrictionsSql(databaseConfig);
    }

    private String parseColumnSql() {
        return String.join(CommonSql.COMMA, MapperFactory.getViewColumnMapperWithAs(this.clazz.getName()).values());
    }

    private String parserModernSql(List<ParameterTranslator> parameters, Map<String, Object> parameterMap) {
        return this.modernList.stream()
                .map(modern -> modern.parse(this.clazz, parameters, parameterMap))
                .collect(Collectors.joining(CommonSql.COMMA));
    }

    private String parseJoinSql() {
        return String.join(CommonSql.BLANK_SPACE, MapperFactory.getJoinSqlList(this.clazz.getName()));
    }

    private String parseRestrictionsSql(DatabaseNodeConfig databaseConfig) {
        List<String> restrictionSqlList = new LinkedList<>();
        for (Restrictions restrictions : this.restrictionsList) {
            restrictionSqlList.add(restrictions.parseSql(this.clazz, databaseConfig));
        }
        return restrictionSqlList.size() > 0 ? CommonSql.WHERE + String.join(CommonSql.AND, restrictionSqlList) : "";
    }

    private String parseOrderSql() {
        if (this.orderList != null && this.orderList.size() > 0) {
            return CommonSql.ORDER_BY +
                    this.orderList.stream()
                            .map(order -> {
                                if (AnnotationType.JOIN.equals(MapperFactory.getAnnotationType(clazz.getName(), order.getSource()))) {
                                    Join join = (Join) MapperFactory.getAnnotation(this.clazz.getName(), order.getSource());
                                    return join.columnSurname() + CommonSql.BLANK_SPACE + order.getSortType();
                                } else {
                                    return MapperFactory.getRepositoryColumnName(this.clazz.getName(), order.getSource()) + CommonSql.BLANK_SPACE + order.getSortType();
                                }
                            })
                            .collect(Collectors.joining(CommonSql.COMMA));
        } else {
            return "";
        }
    }
}
