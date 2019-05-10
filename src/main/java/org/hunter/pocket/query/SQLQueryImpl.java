package org.hunter.pocket.query;

import com.mysql.cj.jdbc.result.ResultSetImpl;
import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.criteria.ParameterTranslator;
import org.hunter.pocket.model.MapperFactory;
import org.hunter.pocket.utils.FieldTypeStrategy;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hunter.pocket.constant.RegexString.SQL_PARAMETER_REGEX;

/**
 * @author wujianchuan 2019/1/3
 */
public class SQLQueryImpl extends AbstractSQLQuery implements SQLQuery {

    private final FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();

    public SQLQueryImpl(String sql, Connection connection) {
        super(sql, connection);
    }

    public SQLQueryImpl(String sql, Connection connection, Class clazz) {
        super(connection, sql, clazz);
    }

    @Override
    public Object unique() throws SQLException {
        ResultSet resultSet = execute(sql);
        if (resultSet.next()) {
            if (clazz != null) {
                try {
                    return getEntity(resultSet);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalAccessError();
                }
            } else {
                return getObjects(resultSet);
            }
        } else {
            return null;
        }
    }

    @Override
    public List list() throws SQLException {
        StringBuilder querySQL = new StringBuilder(this.sql);
        if (this.limited()) {
            querySQL.append(" LIMIT ")
                    .append(this.getStart())
                    .append(CommonSql.COMMA)
                    .append(this.getLimit());
        }
        ResultSet resultSet = execute(querySQL.toString());
        List<Object> results = new ArrayList<>();
        while (resultSet.next()) {
            if (clazz != null) {
                try {
                    Object result = getEntity(resultSet);
                    results.add(result);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalAccessError();
                }
            } else {
                results.add(getObjects(resultSet));
            }
        }
        return results;
    }

    @Override
    public SQLQuery limit(int start, int limit) {
        this.setLimit(start, limit);
        return this;
    }

    @Override
    public SQLQuery setParameter(String key, Object value) {
        this.parameterMap.put(key, value);
        return this;
    }

    @Override
    public SQLQuery mapperColumn(String... columnNames) {
        this.columnNameList.addAll(Arrays.asList(columnNames));
        return this;
    }

    private ResultSet execute(String sql) throws SQLException {
        String executeSql = sql.replaceAll(SQL_PARAMETER_REGEX, CommonSql.PLACEHOLDER);
        PreparedStatement preparedStatement = this.connection.prepareStatement(executeSql);
        if (this.parameterMap.size() > 0) {
            List<ParameterTranslator> queryParameters = new LinkedList<>();
            Pattern pattern = Pattern.compile(SQL_PARAMETER_REGEX);
            Matcher matcher = pattern.matcher(sql);
            while (matcher.find()) {
                String name = matcher.group().substring(1);
                queryParameters.add(ParameterTranslator.newInstance(name, this.parameterMap.get(name)));
            }
            fieldTypeStrategy.setPreparedStatement(preparedStatement, queryParameters);
        }
        return preparedStatement.executeQuery();
    }

    private Map<String, Object> getObjects(ResultSet resultSet) throws SQLException {
        int columnNameSize = this.columnNameList.size();
        int columnSize = ((ResultSetImpl) resultSet).getColumnDefinition().getFields().length;
        if (columnNameSize != columnSize) {
            throw new SQLException("Column mapping failed");
        } else {
            Map<String, Object> result = new LinkedHashMap<>();
            for (int nameIndex = 0, columnIndex = 1; nameIndex < columnNameSize; nameIndex++, columnIndex++) {
                result.put(this.columnNameList.get(nameIndex), resultSet.getObject(columnIndex));
            }
            return result;
        }
    }

    private Object getEntity(ResultSet resultSet) throws InstantiationException, IllegalAccessException {
        Object result = clazz.newInstance();
        List<Field> fields = Arrays.stream(MapperFactory.getRepositoryFields(clazz.getName()))
                .filter(field -> this.sql.contains(field.getName()))
                .collect(Collectors.toList());
        for (Field field : fields) {
            field.set(result, fieldTypeStrategy.getColumnValue(field, resultSet));
        }
        return result;
    }
}
