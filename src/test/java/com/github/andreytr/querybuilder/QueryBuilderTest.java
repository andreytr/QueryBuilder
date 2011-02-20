package com.github.andreytr.querybuilder;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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





    






}
