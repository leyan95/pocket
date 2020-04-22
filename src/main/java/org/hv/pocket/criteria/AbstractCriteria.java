package org.hv.pocket.criteria;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.logger.StatementProxy;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.session.Session;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author wujianchuan 2019/1/10
 */
abstract class AbstractCriteria {
    final StatementProxy statementProxy;
    final Class<? extends AbstractEntity> clazz;
    final Session session;
    final Connection connection;
    final DatabaseNodeConfig databaseConfig;

    List<Restrictions> restrictionsList = new LinkedList<>();
    List<Restrictions> sortedRestrictionsList = new LinkedList<>();
    List<Modern> modernList = new LinkedList<>();
    List<Sort> orderList = new LinkedList<>();
    Map<String, Object> parameterMap = new HashMap<>();
    List<ParameterTranslator> parameters = new LinkedList<>();
    private Integer start;
    private Integer limit;
    StringBuilder completeSql = new StringBuilder();

    AbstractCriteria(Class<? extends AbstractEntity> clazz, Session session) {
        this.clazz = clazz;
        this.session = session;
        this.connection = this.session.getConnection();
        this.databaseConfig = this.session.getDatabaseNodeConfig();
        this.statementProxy = StatementProxy.newInstance(this.databaseConfig);
    }

    public Session getSession() {
        return session;
    }

    void cleanAll() {
        this.cleanWithoutRestrictions();
        this.cleanRestrictions();
    }

    void cleanWithoutRestrictions() {
        modernList = new LinkedList<>();
        orderList = new LinkedList<>();
        parameterMap = new HashMap<>(16);
        parameters = new LinkedList<>();
        start = null;
        limit = null;
        completeSql = new StringBuilder();
    }

    void cleanRestrictions() {
        sortedRestrictionsList = new LinkedList<>();
        this.restrictionsList = new LinkedList<>();
    }

    void setLimit(int start, int limit) {
        this.start = start;
        this.limit = limit;
    }

    boolean limited() {
        return this.start != null && this.limit != null;
    }

    Integer getStart() {
        return start;
    }

    Integer getLimit() {
        return limit;
    }
}
