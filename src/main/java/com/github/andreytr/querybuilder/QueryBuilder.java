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
 * QueryBuilder for JPA. Allow use statements:
 * <ul>
 *  <li>SELECT
 *  <li>FROM
 *  <li>JOIN/INNER JOIN/LEFT JOIN/LEFT OUTER JOIN [FETCH]
 *  <li>WHERE
 *  <li>GROUP BY
 *  <li>HAVING
 *  <li>ORDER BY
 * </ul>
 */
public class QueryBuilder implements Cloneable {
    private static final String DEFAULT_ALIAS = "e";

    private String paramPreffix = "param";

    private String select = DEFAULT_ALIAS;
    private String from;
    private String orderBy;
    private String groupBy;
    private String having;
    private List<String> joinLst = new ArrayList<String>();
    private List<String> andWheres = new ArrayList<String>();
    private List<String> orWheres = new ArrayList<String>();
    private Map<String, Object> paramsMap = new HashMap<String, Object>();

    public QueryBuilder(String from) {
        this.from = from;
    }

    public QueryBuilder(Class clazz, String alias) {
        this(clazz.getSimpleName() + " " + alias);
        select = alias;
    }

    public QueryBuilder(Class clazz) {
        this(clazz, DEFAULT_ALIAS);
    }

    public String getQuery() {
        StringBuilder result = new StringBuilder()
                               .append("SELECT ").append(select)
                               .append(" FROM ").append(from);
        for(String joinStatement: joinLst) {
            result.append(" ").append(joinStatement);
        }
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
        if (groupBy != null) {
            result.append(" GROUP BY ").append(groupBy);
        }
        if (having != null) {
            result.append(" HAVING ").append(having);
        }
        if (orderBy != null) {
            result.append(" ORDER BY ").append(orderBy);
        }
        return result.toString();
    }

    public Map<String, Object> getParamsMap() {
        return paramsMap;
    }

    public QueryBuilder select(String select) {
        this.select = select;
        return getClone();
    }

    public QueryBuilder join(String joinStatement) {
        joinLst.add("JOIN " + joinStatement);
        return getClone();
    }

    public QueryBuilder joinFetch(String joinStatement) {
        joinLst.add("JOIN FETCH " + joinStatement);
        return getClone();
    }

    public QueryBuilder innerJoin(String joinStatement) {
        joinLst.add("INNER JOIN " + joinStatement);
        return getClone();
    }

    public QueryBuilder innerJoinFetch(String joinStatement) {
        joinLst.add("INNER JOIN FETCH " + joinStatement);
        return getClone();
    }

    public QueryBuilder leftJoin(String joinStatement) {
        joinLst.add("LEFT JOIN " + joinStatement);
        return getClone();
    }

    public QueryBuilder leftJoinFetch(String joinStatement) {
        joinLst.add("LEFT JOIN FETCH " + joinStatement);
        return getClone();
    }

    public QueryBuilder leftOuterJoin(String joinStatement) {
        joinLst.add("LEFT OUTER JOIN " + joinStatement);
        return getClone();
    }

    public QueryBuilder leftOuterJoinFetch(String joinStatement) {
        joinLst.add("LEFT OUTER JOIN FETCH " + joinStatement);
        return getClone();
    }

    public QueryBuilder andWhere(String where, Object... params) {
        checkParamCount(where, params);
        int nextParamNumber = getNextParamNumber();
        andWheres.add(insertNamedParamInStatement(where, nextParamNumber));
        addParams(nextParamNumber, params);
        return getClone();
    }

    public QueryBuilder andWhereIfNotNull(String where, Object param) {
        if (param == null) {
            return getClone();
        }
        return andWhere(where, param);
    }

    public QueryBuilder orWhere(String where, Object... params) {
        checkParamCount(where, params);
        int nextParamNumber = getNextParamNumber();
        orWheres.add(insertNamedParamInStatement(where, nextParamNumber));
        addParams(nextParamNumber, params);
        return getClone();
    }

    public QueryBuilder orWhereIfNotNull(String where, Object param) {
        if (param == null) {
            return getClone();
        }
        return orWhere(where, param);
    }

    public QueryBuilder orderBy(String orderBy) {
        this.orderBy = orderBy;
        return getClone();
    }

    public QueryBuilder groupBy(String groupBy) {
        this.groupBy = groupBy;
        return getClone();
    }

    public QueryBuilder having(String having) {
        this.having = having;
        return getClone();
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
        while(statement.indexOf('?') > -1) {
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

    private QueryBuilder getClone() {
        try {
            return (QueryBuilder)clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
