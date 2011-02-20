package com.github.andreytr.querybuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: andreytr
 * Date: 20.02.2011
 * Time: 21:25:05
 *
 *
 * SELECT
 * FROM
 */
public class QueryBuilder {

    private String paramPreffix = "param";

    private String select = "e";
    private String from;
    private List<String> andWheres = new ArrayList<String>();
    private List<String> orWheres = new ArrayList<String>();
    private Map<String, Object> paramsMap = new HashMap<String, Object>();

    public QueryBuilder(String from) {
        this.from = from;
    }

    public String getQuery() {
        StringBuilder result = new StringBuilder()
                               .append("SELECT ").append(select)
                               .append(" FROM ").append(from);
        if (andWheres.size() > 0 || orWheres.size() > 0) {
            result.append(" WHERE ");
        }
        if (andWheres.size() > 0) {
            result.append(concatWith(andWheres, "AND"));
        }
        if (orWheres.size() > 0) {
            if (andWheres.size() > 0) {
                result.append(" OR ");
            }
            result.append(concatWith(orWheres, "OR"));
        }
        return result.toString();
    }

    public Map<String, Object> getParamsMap() {
        return paramsMap;
    }

    public QueryBuilder select(String select) {
        this.select = select;
        return this;
    }

    public QueryBuilder andWhere(String where, Object... params) {
        checkParamCount(where, params);
        int nextParamNumber = getNextParamNumber();
        andWheres.add(insertNamedParamInStatement(where, nextParamNumber));
        addParams(nextParamNumber, params);
        return this;
    }

    public QueryBuilder andWhereIfNotNull(String where, Object param) {
        if (param == null) {
            return this;
        }
        return andWhere(where, param);
    }

    public QueryBuilder orWhere(String where, Object... params) {
        checkParamCount(where, params);
        int nextParamNumber = getNextParamNumber();
        orWheres.add(insertNamedParamInStatement(where, nextParamNumber));
        addParams(nextParamNumber, params);
        return this;
    }

    public QueryBuilder orWhereIfNotNull(String where, Object param) {
        if (param == null) {
            return this;
        }
        return orWhere(where, param);
    }

    private void checkParamCount(String statement, Object... params) {
        int startWith = 0;
        for(Object param:params) {
            int i = statement.indexOf("?", startWith);
            if (i < 0) {
                throw new IllegalArgumentException("Param count!");
            }
            startWith = i + 1;
        }
        int i = statement.indexOf("?", startWith);
        if (i > -1) {
            throw new IllegalArgumentException("Param count!");
        }
    }

    private int getNextParamNumber() {
        return paramsMap.size() + 1;
    }

    private String insertNamedParamInStatement(String statement, int startSuffix) {
        int i;
        while((i = statement.indexOf('?')) > -1) {
            statement = statement.replaceFirst("\\?", ":" + paramPreffix + startSuffix);
            startSuffix++;
        }
        return statement;
    }

    private StringBuilder concatWith(List<String> statements, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for(String statement:statements) {
            if (builder.length() > 0) {
                builder.append(" ").append(delimiter).append(" ");
            }
            builder.append("(").append(statement).append(")");
        }
        return builder;
    }

    private void addParams(int startSuffix, Object... params) {
        for(Object param:params) {
            paramsMap.put(paramPreffix + startSuffix, param);
            startSuffix++;
        }
    }
}
