package org.hv.pocket.criteria;

import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.exception.CriteriaException;
import org.hv.pocket.exception.ErrorMessage;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hv.pocket.constant.RegexString.EL_FIELD_REGEX;
import static org.hv.pocket.constant.RegexString.SQL_PARAMETER_REGEX;

/**
 * @author wujianchuan 2019/1/21
 */
public class Modern implements SqlBean {
    private final Pattern fieldPattern = Pattern.compile(EL_FIELD_REGEX);
    private final Pattern valuePattern = Pattern.compile(SQL_PARAMETER_REGEX);

    private String source;
    private Object target;
    private String poEl;
    private final Boolean withPoEl;

    private Modern(String source, Object target) {
        this.source = source;
        this.target = target;
        this.withPoEl = false;
    }

    private Modern(String poEl) {
        this.poEl = poEl;
        this.withPoEl = true;
    }

    public static Modern set(String source, Object target) {
        return new Modern(source, target);
    }


    public static Modern setWithPoEl(String poEl) {
        return new Modern(poEl);
    }

    public String getSource() {
        return source;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    public String getPoEl() {
        return this.poEl;
    }

    private Boolean getWithPoEl() {
        return withPoEl;
    }

    String parse(Class<? extends AbstractEntity> clazz, List<ParameterTranslator> parameters, Map<String, Object> parameterMap) {
        if (this.getWithPoEl()) {
            String sql = poEl;
            Matcher fieldMatcher = fieldPattern.matcher(this.poEl);
            Matcher valueMatcher = valuePattern.matcher(this.poEl);
            String fieldName = null;
            try {
                while (fieldMatcher.find()) {
                    fieldName = fieldMatcher.group().substring(1);
                    sql = sql.replace(fieldMatcher.group(), MapperFactory.getRepositoryColumnName(clazz.getName(), fieldName));
                }
            } catch (NullPointerException e) {
                throw new CriteriaException(String.format(ErrorMessage.POCKET_ILLEGAL_FIELD_EXCEPTION, fieldName));
            }
            while (valueMatcher.find()) {
                sql = sql.replace(valueMatcher.group(), CommonSql.PLACEHOLDER);
                parameters.add(ParameterTranslator.newInstance(parameterMap.get(valueMatcher.group().substring(1))));
            }
            return sql;
        } else {
            parameters.add(ParameterTranslator.newInstance(this.target));
            return MapperFactory.getRepositoryColumnName(clazz.getName(), this.getSource()) + CommonSql.EQUAL_TO + CommonSql.PLACEHOLDER;
        }
    }
}
