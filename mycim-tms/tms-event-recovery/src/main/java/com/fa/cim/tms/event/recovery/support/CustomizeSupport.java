package com.fa.cim.tms.event.recovery.support;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimReflectionUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.tms.event.recovery.entity.BaseEntity;
import com.fa.cim.tms.event.recovery.entity.NonRuntimeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Customize support utils.
 *
 * <p> Support for native SQL statement execution.
 *
 * <p> Please use this utils to CRUD operation when execution entity is extends {@link NonRuntimeEntity}.
 * <br/> - add/update method: {@link CustomizeSupport#saveNonRuntimeEntity(NonRuntimeEntity)}
 * <br/> - remove method: {@link CustomizeSupport#removeNonRuntimeEntity(NonRuntimeEntity)}
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/8/13 12:29
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Component
@Slf4j
public class CustomizeSupport {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get unique element.
     *
     * @param list list of Object
     * @return unique element
     * @version 1.0
     * @author ZQI
     * @date 2019/8/13 11:11
     */
    private static <R> R uniqueElement(List<R> list) {
        if (CimArrayUtils.isEmpty(list)) {
            return null;
        }
        int size = list.size();
        R first = list.get(0);
        for (int i = 1; i < size; i++) {
            if (!list.get(i).equals(first)) {
                throw new NonUniqueResultException();
            }
        }
        return first;
    }

    /**
     * Query a info from DB by a example of entity.
     *
     * @param <T>     example entity
     * @param example instance of {@link BaseEntity}
     * @return a record from DB
     * @version 1.0
     * @author ZQI
     * @date 2020/3/12 12:34
     */
    public <T extends BaseEntity> T findOne(T example) {
        Assert.notNull(example, "The query object cannot be null.");
        List<T> results = this.findAll(example);
        if (CimArrayUtils.getSize(results) > 1) {
            throw new NonUniqueResultException();
        }
        if (CimArrayUtils.isNotEmpty(results)) {
            return results.get(0);
        }
        return null;
    }

    public <T extends BaseEntity> List<T> findAll(Class<T> aClass) {
        Table table = aClass.getAnnotation(Table.class);
        Assert.notNull(table, String.format("The DO[%s] is not a Entity.", aClass.getSimpleName()));
        String tableName = table.name();

        Field[] fields = aClass.getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        AtomicBoolean isFirst = new AtomicBoolean(true);
        for (Field field : fields) {
            ReflectionUtils.makeAccessible(field);
            String columnName = Optional.ofNullable(field.getAnnotation(Column.class)).map(Column::name).orElse("");
            if (CimStringUtils.isNotEmpty(columnName)) {
                sb.append(isFirst.getAndSet(false) ? "" : ", ").append(columnName);
            }
        }

        if (sb.length() < 1) {
            return Collections.emptyList();
        }

        String sql = "SELECT ID, " + sb.append(" FROM ").append(tableName).toString();
        log.debug(">>> SQL: " + sql);
        Query nativeQuery = entityManager.createNativeQuery(sql, aClass);
        return new ArrayList<>(nativeQuery.getResultList());
    }

    /**
     * Query all record from DB by a example of entity.
     *
     * @param <T>     example entity
     * @param example instance of {@link BaseEntity}
     * @return list of record from DB
     * @version 1.0
     * @author ZQI
     * @date 2020/3/12 12:36
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> List<T> findAll(T example) {
        Assert.notNull(example, "The query object cannot be null.");
        Class<? extends BaseEntity> aClass = example.getClass();

        Table table = aClass.getAnnotation(Table.class);
        Assert.notNull(table, String.format("The DO[%s] is not a Entity.", aClass.getSimpleName()));

        String tableName = table.name();
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        sql.append("SELECT * FROM ").append(tableName).append(" WHERE 1=1");

        List<Field> fields = CimReflectionUtils.allFields(example.getClass());
        int length = fields.size();
        int index = 0;
        if (length > 0) {
            for (; ; ) {
                Field field = fields.get(index);
                if (Modifier.isStatic(field.getModifiers())) {
                    index++;
                    continue;
                }

                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, example);
                if (null != value) {
                    sql.append(" AND ");
                    Column column = field.getAnnotation(Column.class);
                    if (null == column) {
                        sql.append(field.getName());
                    } else {
                        sql.append(column.name());
                    }
                    sql.append(" = ?");
                    parameters.add(value);
                }
                if (index + 1 == length) {
                    break;
                }
                index++;
            }
        }
        if (parameters.size() == 0) {
            return Collections.emptyList();
        }

        log.debug(">>> SQL: " + sql);
        Query nativeQuery = entityManager.createNativeQuery(sql.toString(), example.getClass());
        index = 0;
        for (Object parameter : parameters) {
            nativeQuery.setParameter(++index, parameter);
        }
        return new ArrayList<T>(nativeQuery.getResultList());
    }

    /**
     * <p>Execute a SELECT query and return the query results as an untyped List.
     *
     * @param nativeSql native SQl
     * @param params    dynamic query condition
     * @return a list of result.
     * @version 1.0
     * @author ZQI
     * @date 2019/11/25 12:20
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> query(String nativeSql, Object... params) {
        List resultList = generateQuery(nativeSql, params).getResultList();
        Optional.ofNullable(resultList).ifPresent(list -> {
            int i = 0;
            for (Object o : list) {
                if (!(o instanceof Object[])) {
                    Object[] objects = new Object[1];
                    objects[0] = o;
                    list.set(i, objects);
                }
                i++;
            }
        });
        return resultList;
    }


    public List<Object> oneResultListQuery(String nativeSql, Object... params) {
        return new ArrayList<Object>(generateQuery(nativeSql, params).getResultList());
    }

    public Object oneResultQuery(String nativeSql, Object... params) {
        List resultList = generateQuery(nativeSql, params).getResultList();
        if (CimArrayUtils.isEmpty(resultList)) {
            return null;
        }
        return resultList.get(0);
    }

    /**
     * Execute a SELECT query and return the count of query results.
     *
     * @param nativeSql native SQL
     * @param params    dynamic query condition
     * @return count of results.
     * @version 1.0
     * @author ZQI
     * @date 2019/8/13 12:42
     */
    public long count(String nativeSql, Object... params) {
        List<?> result = generateQuery(nativeSql, params).getResultList();
        if (CimArrayUtils.isEmpty(result)) {
            return 0;
        }
        if (result.size() > 1) {
            throw new NonUniqueResultException("count query not return a single result");
        }
        Number count = (Number) result.get(0);
        return null == count ? 0 : count.longValue();
    }

    /**
     * Execute a SELECT query and return the query results as an typed List.
     *
     * @param nativeSql native SQL
     * @param classType mapping entity for Hibernate ORM
     * @param params    dynamic query condition
     * @param <T>       entity mapped by ORM
     * @return a list of entity that mapped by ORM.
     * @version 1.0
     * @author ZQI
     * @date 2019/8/13 12:14
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> query(String nativeSql, Class<T> classType, Object... params) {
        return (List<T>) getNativeQuery(nativeSql, classType, params).getResultList();
    }

    private <T> Query getNativeQuery(String nativeSql, Class<T> classType, Object[] params) {
        String upperCaseSql = nativeSql.toUpperCase();
        Validations.check(!upperCaseSql.contains("FROM"), "SQL symbol error");
        int fromIndex = upperCaseSql.indexOf("FROM");
        String temp = nativeSql.substring(0, fromIndex + 4);
        String querySql = String.format("%s %s", temp.toUpperCase(), nativeSql.substring(fromIndex + 4));

        //if not contains *, then replace to *
        String displayColumns = querySql.substring(querySql.indexOf("SELECT") + 6, querySql.indexOf("FROM"));
        if (!displayColumns.contains(BizConstant.SP_ADCSETTING_ASTERISK)) {
            String preSql = querySql.substring(0, querySql.indexOf("SELECT") + 6);
            String postSql = querySql.substring(querySql.indexOf("FROM"));
            querySql = String.format("%s * %s", preSql, postSql);
            if (displayColumns.contains(BizConstant.DOT)) {
                String prefix = displayColumns.substring(0, displayColumns.indexOf(BizConstant.DOT));
                querySql = String.format("%s %s.* %s", preSql, prefix.trim(), postSql);
            }
        }
        Query nativeQuery = entityManager.createNativeQuery(querySql, classType);
        setParams(nativeQuery, params);
        return nativeQuery;
    }

    private long countTotal(String nativeSql, Object... params) {
        String countSql = String.format("SELECT COUNT(*) FROM (%s) ct", nativeSql);
        return this.count(countSql, params);
    }

    /**
     * Get one result from execute native SQL.
     *
     * @param nativeSql native SQL
     * @param params    dynamic query condition
     * @return array of object
     * @version 1.0
     * @author ZQI
     * @date 2019/8/13 11:11
     */
    public Object[] queryOne(String nativeSql, Object... params) {
        return uniqueElement(query(nativeSql, params));
    }

    /**
     * Get one result from execute native SQL and mapped entity by ORM.
     *
     * @param nativeSql native SQL
     * @param classType mapping entity for Hibernate ORM
     * @param params    dynamic query condition
     * @param <T>       entity mapped by ORM
     * @return result that execute SQL query
     * @version 1.0
     * @author ZQI
     * @date 2019/8/13 11:11
     */
    public <T> T queryOne(String nativeSql, Class<T> classType, Object... params) {
        return uniqueElement(query(nativeSql, classType, params));
    }

    private Query generateQuery(String nativeSql, Object... params) {
        Query nativeQuery = entityManager.createNativeQuery(nativeSql);
        setParams(nativeQuery, params);
        return nativeQuery;
    }

    private void setParams(Query nativeQuery, Object... params) {
        if (null != params) {
            int size = params.length;
            if (size > 0) {
                for (int position = 0; position < size; position++) {
                    nativeQuery.setParameter(position + 1, params[position]);
                }
            }
        }
    }

    /**
     * Save/Update non runtime entity.
     *
     * @param <S>    object that extends {@link NonRuntimeEntity}
     * @param entity need save/update entity. Must extends {@link NonRuntimeEntity}
     * @return {@link S}
     * @version 1.0
     * @author ZQI
     * @date 2019/8/13 11:11
     */
    public <S extends NonRuntimeEntity> S saveNonRuntimeEntity(S entity) {
        if (CimStringUtils.isEmpty(entity.getId())) {
            entity.setId(SnowflakeIDWorker.getInstance().generateId(entity.getClass()));
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }

    /**
     * Remove non runtime entity.
     *
     * @param <S>    object that extends {@link NonRuntimeEntity}
     * @param entity need removed entity. Must extends {@link NonRuntimeEntity}
     * @version 1.0
     * @author ZQI
     * @date 2019/8/13 11:11
     */
    public <S extends NonRuntimeEntity> void removeNonRuntimeEntity(S entity) {
        Assert.notNull(entity, "The parameter cannot be null.");
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    /**
     * Remove non runtime entity by Spec example entity.
     *
     * @param <S>     object that extends {@link NonRuntimeEntity}
     * @param example example entity. Must extends {@link NonRuntimeEntity}
     * @version 1.0
     * @author ZQI
     * @date 2019/8/13 11:11
     */
    public <S extends NonRuntimeEntity> void removeNonRuntimeEntityForExample(S example) {
        Assert.notNull(example, "The example cannot be null.");
        Optional.ofNullable(findAll(example)).ifPresent(list -> list.forEach(this::removeNonRuntimeEntity));
    }
}