package com.github.andreytr.querybuilder;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * User: andreytr
 * Date: 20.02.2011
 * Time: 21:27:21
 */
@Test
public class QueryBuilderTest {

    private QueryBuilder builder;

    @BeforeMethod
    public void setUp() {
        builder = new QueryBuilder("Entity e");
    }

    public void emptySelect() {
        assertEquals(builder.getQuery(), "SELECT e FROM Entity e");
        assertEquals(new HashMap(), builder.getParamsMap());
    }

    public void emptySelectByClass() {
        builder = new QueryBuilder(Object.class);
        assertEquals(builder.getQuery(), "SELECT e FROM Object e");
        assertEquals(new HashMap(), builder.getParamsMap());

        builder = new QueryBuilder(Object.class, "o");
        assertEquals(builder.getQuery(), "SELECT o FROM Object o");
        assertEquals(new HashMap(), builder.getParamsMap());
    }

    public void select() {
        builder.select("e.field1, e.field2");

        assertEquals(builder.getQuery(), "SELECT e.field1, e.field2 FROM Entity e");
        assertEquals(new HashMap(), builder.getParamsMap());
    }

    @Test(
        expectedExceptions = IllegalArgumentException.class
    )
    public void andWhereParamCount() {
        builder.andWhere("e.name = ? or e.name = ?", "param1", "param2", "param3");
    }

    @Test(
        expectedExceptions = IllegalArgumentException.class
    )
    public void andWhereParamCountLess() {
        builder.andWhere("e.name = ? or e.name = ?");
    }

    public void andWhere() {
        builder.andWhere("e.name = ?", "sample");

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e WHERE (e.name = :param1)");
        Map<String, Object> params = builder.getParamsMap();
        assertNotNull(params);
        assertEquals(params.size(), 1);
        assertEquals(params.get("param1"), "sample");
    }

    public void andWhereSecond() {
        builder.andWhere("e.name = ?", "sample")
               .andWhere("e.companyId = ?", 12);

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e WHERE (e.name = :param1) AND (e.companyId = :param2)");
        Map<String, Object> params = builder.getParamsMap();
        assertNotNull(params);
        assertEquals(params.size(), 2);
        assertEquals(params.get("param1"), "sample");
        assertEquals(params.get("param2"), 12);
    }

    public void orWhere() {
        builder.orWhere("e.name = ?", "sample");

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e WHERE (e.name = :param1)");
        Map<String, Object> params = builder.getParamsMap();
        assertNotNull(params);
        assertEquals(params.size(), 1);
        assertEquals(params.get("param1"), "sample");
    }

    public void orWhereSecond() {
        builder.orWhere("e.name = ?", "sample")
               .orWhere("e.companyId = ?", 12);

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e WHERE (e.name = :param1) OR (e.companyId = :param2)");
        Map<String, Object> params = builder.getParamsMap();
        assertNotNull(params);
        assertEquals(params.size(), 2);
        assertEquals(params.get("param1"), "sample");
        assertEquals(params.get("param2"), 12);
    }

    public void andOrWhere() {
        builder.andWhere("e.name = ?", "sample")
               .andWhere("e.accountId = ?", 5)
               .orWhere("e.companyId = ?", 12);

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e WHERE (e.name = :param1) AND (e.accountId = :param2) OR (e.companyId = :param3)");
        Map<String, Object> params = builder.getParamsMap();
        assertNotNull(params);
        assertEquals(params.size(), 3);
        assertEquals(params.get("param1"), "sample");
        assertEquals(params.get("param2"), 5);
        assertEquals(params.get("param3"), 12);
    }

    public void andWhereWithoutParams() {
        builder.andWhere("e.name is null");

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e WHERE (e.name is null)");
        assertEquals(new HashMap(), builder.getParamsMap());
    }

    public void andWhereIfNotNullForNull() {
        builder.andWhereIfNotNull("e.field1 = ?", null);

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e");
        assertEquals(new HashMap(), builder.getParamsMap());
    }

    public void andWhereIfNotNull() {
        builder.andWhereIfNotNull("e.field1 = ?", "value");

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e WHERE (e.field1 = :param1)");
        Map<String, Object> params = builder.getParamsMap();
        assertNotNull(params);
        assertEquals(params.size(), 1);
        assertEquals(params.get("param1"), "value");
    }

    public void orWhereIfNotNullForNull() {
        builder.orWhereIfNotNull("e.field1 = ?", null);

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e");
        assertEquals(new HashMap(), builder.getParamsMap());
    }

    public void orWhereIfNotNull() {
        builder.orWhereIfNotNull("e.field1 = ?", "value");

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e WHERE (e.field1 = :param1)");
        Map<String, Object> params = builder.getParamsMap();
        assertNotNull(params);
        assertEquals(params.size(), 1);
        assertEquals(params.get("param1"), "value");
    }

    public void orderBy() {
        builder.orderBy("e.field ASC");

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e ORDER BY e.field ASC");
        assertEquals(new HashMap(), builder.getParamsMap());
    }

    public void groupBy() {
        builder.groupBy("e.field");

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e GROUP BY e.field");
        assertEquals(new HashMap(), builder.getParamsMap());
    }

    public void having() {
        builder.having("e.field DESC");

        assertEquals(builder.getQuery(), "SELECT e FROM Entity e HAVING e.field DESC");
        assertEquals(new HashMap(), builder.getParamsMap());
    }

    public void sampleQuery() {
        String productName = "sample";
        String userName    = null;
        builder = new QueryBuilder("Order o")
                  .andWhere("o.name in ?", Arrays.asList("sample1", "sample2"))
                  .andWhere("o.date > ? OR o.date < ?", 12, 5)
                  .andWhereIfNotNull("o.product = ?", productName)
                  .andWhereIfNotNull("o.user = ?", userName);

        String selectQuery = builder.select("o.id, o.name, o.user, o.product")
                                    .orderBy("o.name")
                                    .getQuery();

        String countQuery  = builder.select("COUNT(o)")
                                    .getQuery();

        Map<String, Object> params = builder.getParamsMap();


        assertEquals(selectQuery, "SELECT o.id, o.name, o.user, o.product " +
                                  "FROM Order o " +
                                  "WHERE (o.name in :param1) AND " +
                                        "(o.date > :param2 OR o.date < :param3) AND " +
                                        "(o.product = :param4) " +
                                  "ORDER BY o.name");
        assertEquals(countQuery,  "SELECT COUNT(o) " +
                                  "FROM Order o " +
                                  "WHERE (o.name in :param1) AND " +
                                        "(o.date > :param2 OR o.date < :param3) AND " +
                                        "(o.product = :param4)");
        assertNotNull(params);
        assertEquals(params.size(), 4);
        assertEquals(params.get("param1"), Arrays.asList("sample1", "sample2"));
        assertEquals(params.get("param2"), 12);
        assertEquals(params.get("param3"), 5);
        assertEquals(params.get("param4"), "sample");

    }
}
