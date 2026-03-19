package com.ttn.ck.queryprocessor.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ttn.ck.queryprocessor.aop.QueryProcessor;
import com.ttn.ck.queryprocessor.enums.NumberEnum;
import com.ttn.ck.queryprocessor.exception.RowIndexMandatory;
import com.ttn.ck.queryprocessor.function.RowMapper;
import com.ttn.ck.queryprocessor.utils.Instance;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.query.NativeQuery;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Amit Raturi
 * Query Executor framework is based on Entity manager bean, and it provides utilty functions/methods to fetch data and map data to respective DTO's.
 * Query Executor is used to process native queries, to process namedQueries , You can use original entities na repos to bind them.
 * This class is thread safe.
 */
@Slf4j
public class QueryExecutor {
    private static QueryManager queryManager;
    private static ObjectMapper mapper;

    private QueryExecutor() {
    }

    /**
     * This method executes query using entity manager and binds result to a LinkedHashMap , sequence of the result will depend on sequence of parameter "fields", It uses filed name as key to fetch result.
     * This Method take more memory as it internally uses Tuples to map data to original objects. use getResultByIndex for more optimized approach , as it directly map object with index.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param fields, column named to be replaced
     * @return will return "List<LinkedHashMap<String, Object>>"
     */
    public static List<LinkedHashMap<String, Object>> getResult(String qString, Map<String, Object> params, List<String> fields) {
        return getResultMap(qString, params, fields);
    }

    /**
     * This method executes query using entity manager and binds results to class Instance provided as "convertTo Class<T>"
     * This Method take more memory as it internally uses Tuples to map data to original objects. use getResultByIndex for more optimized approach , as it directly map object with index.
     *
     * @param qString   native query String.
     * @param params    query params, these params will get replaced in the query.
     * @param convertTo class Instance to be returned.
     * @param <T>       Generic Type to be returned.
     * @return List of Generic type <T> which is passed as "convertTo".
     */
    public static <T> List<T> getResult(String qString, Map<String, Object> params, Class<T> convertTo) {
        List<Field> fields = getFields(convertTo);
        List<String> sFields = new LinkedList<>();
        for (Field field : fields) {
            sFields.add(getFieldName(field));
        }
        return getResultObject(qString, params, convertTo, sFields);
    }

    /**
     * This method executes query using entity manager and binds results to class Instance using lambda function "rowMapper" of Type "RowMapper".
     *
     * @param qString   native query String.
     * @param params    query params, these params will get replaced in the query.
     * @param rowMapper lambda function of type "RowMapper" used to bind object array to class Instance.
     * @param <T>       Generic Type to be returned from lambda function "rowMapper".
     * @param <R>       Generic Type to be returned from query result, and input for lambda function "rowMapper".
     * @return List of Generic type <T> which is passed as "convertTo".
     */
    public static <T, R> List<T> getResult(String qString, Map<String, Object> params, RowMapper<T, R> rowMapper) {
        Query query = getQuery(qString, params);
        List<R> qRows = getResultList(query);
        return convertTo(qRows, rowMapper);
    }

    /**
     * This method executes query using entity manager and binds results to class Instance using value and index value of JsonProperty. ie:
     * <div> public class DummyObject {
     *     @JsonProperty(value = "ZERO", index = 0)
     *     private String column1;
     *     @JsonProperty(value = "ONE", index = 1)
     *     private BigDecimal column2;
     *     }
     * </div>
     * Now Object array returned by entity manger will get bind to   "@JsonProperty" index and value.
     * NOTE: both index and values are mandatory and should have one to one mapping with index to its number spelling in english, value should be n caps and in case if there is spelling mistake, index to value mapping is wrong(ie: value="Zero", index 1) data mapping will get inconsistent.
     *
     * @param qString   native query String.
     * @param params    query params, these params will get replaced in the query.
     * @param convertTo class Instance to be returned.
     * @param <T>       Generic Type to be returned.
     * @return List of Generic type <T> which is passed as "convertTo".
     */
    public static <T> List<T> getResultByIndex(String qString, Map<String, Object> params, Class<T> convertTo) {
        List<Field> fields = getFields(convertTo);
        List<Integer> iFields = new LinkedList<>();
        for (Field field : fields) {
            iFields.add(getFieldIndex(field));
        }
        return getResultAndMapWithIndex(qString, params, convertTo, iFields);
    }

    /**
     * This method executes query using entity manager and binds result to a LinkedHashMap , sequence of the result will depend on sequence of parameter "fields", note it uses field index as keys to fetch results.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param fields, column named to be replaced.
     * @return will return "List<LinkedHashMap<String, Object>>".
     */
    public static List<Map<String, Object>> getResultByIndex(String qString, Map<String, Object> params, List<String> fields) {
        return getResultMapWithIndex(qString, params, fields);
    }

    /**
     * This method executes query using entity manager and bind result to entity class "eClass".
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param eClass  entity class Instance to be returned.
     * @param <T>     Generic Type to be returned.
     * @return List of Generic type <T> which is passed as "eClass".
     */
    public static <T> List<T> getEntityResult(String qString, Map<String, Object> params, Class<T> eClass) {
        Query query = getQuery(qString, params, eClass);
        return getResultList(query);
    }


    /**
     * This method executes query using entity manager and binds result to a LinkedHashMap , sequence of the result will depend on sequence of parameter "fields", It uses filed name as key to fetch result.
     * This Method take more memory as it internally uses Tuples to map data to original objects. use getResultByIndex for more optimized approach , as it directly map object with index.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param fields, column named to be replaced
     * @return will return "Map<String, Object>"
     */
    public static Map<String, Object> getSingleResult(String qString, Map<String, Object> params, List<String> fields) {
        return getSingleResultMapWithIndex(qString, params, fields);
    }

    /**
     * This method executes query using entity manager and binds results to class Instance provided as "convertTo Class<T>"
     * This Method take more memory as it internally uses Tuples to map data to original objects. use getResultByIndex for more optimized approach , as it directly map object with index.
     *
     * @param qString   native query String.
     * @param params    query params, these params will get replaced in the query.
     * @param convertTo class Instance to be returned.
     * @param <T>       Generic Type to be returned.
     * @return Generic type <T> which is passed as "convertTo".
     */
    public static <T> T getSingleResult(String qString, Map<String, Object> params, Class<T> convertTo) {
        List<Field> fields = getFields(convertTo);
        List<String> sFields = new LinkedList<>();
        for (Field field : fields) {
            sFields.add(getFieldName(field));
        }
        return getSingleResultObject(qString, params, convertTo, sFields);
    }

    /**
     * This method executes query using entity manager and binds results to class Instance using lambda function "rowMapper" of Type "RowMapper".
     *
     * @param qString   native query String.
     * @param params    query params, these params will get replaced in the query.
     * @param rowMapper lambda function of type "RowMapper" used to bind object array to class Instance.
     * @param <T>       Generic Type to be returned from lambda function "rowMapper".
     * @param <R>       Generic Type to be returned from query result, and input for lambda function "rowMapper".
     * @return Generic type <T> which is passed as "convertTo".
     */
    public static <T, R> T getSingleResult(String qString, Map<String, Object> params, RowMapper<T, R> rowMapper) {
        Query query = getQuery(qString, params);
        R qRow = getSingleResult(query);
        return convertTo(qRow, rowMapper);
    }

    /**
     * This method executes query using entity manager and binds results to class Instance using value and index value of JsonProperty. ie:
     * <div> public class DummyObject {
     *     @JsonProperty(value = "ZERO", index = 0)
     *     private String column1;
     *     @JsonProperty(value = "ONE", index = 1)
     *     private BigDecimal column2;
     *     }
     * </div>
     * Now Object array returned by entity manger will get bind to   "@JsonProperty" index and value.
     * NOTE: both index and values are mandatory and should have one to one mapping with index to its number spelling in english, value should be n caps and in case if there is spelling mistake, index to value mapping is wrong(ie: value="Zero", index 1) data mapping will get inconsistent.
     *
     * @param qString   native query String.
     * @param params    query params, these params will get replaced in the query.
     * @param convertTo class Instance to be returned.
     * @param <T>       Generic Type to be returned.
     * @return Generic type <T> which is passed as "convertTo".
     */
    public static <T> T getSingleResultByIndex(String qString, Map<String, Object> params, Class<T> convertTo) {
        List<Field> fields = getFields(convertTo);
        List<Integer> iFields = new LinkedList<>();
        for (Field field : fields) {
            iFields.add(getFieldIndex(field));
        }
        return getSingleResultAndMapWithIndex(qString, params, convertTo, iFields);
    }

    /**
     * This method executes query using entity manager and binds result to a LinkedHashMap , sequence of the result will depend on sequence of parameter "fields", note it uses field index as keys to fetch results.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param fields, column named to be replaced.
     * @return will return "Map<String, Object>".
     */
    public static Map<String, Object> getSingleResultByIndex(String qString, Map<String, Object> params, List<String> fields) {
        return getSingleResultMapWithIndex(qString, params, fields);
    }

    /**
     * This method executes query using entity manager and bind result to entity class "eClass" and return single result.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param eClass  entity class Instance to be returned.
     * @param <T>     Generic Type to be returned.
     * @return Generic type <T> which is passed as "eClass".
     */
    public static <T> T getSingleEntityResult(String qString, Map<String, Object> params, Class<T> eClass) {
        Query query = getQuery(qString, params, eClass);
        return getSingleResult(query);
    }

    /**
     * This method executes query and return raw results.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param <T>     Generic Type to be returned.
     * @return single raw result.
     */
    public static <T> T getSingleResult(String qString, Map<String, Object> params) {
        Query query = getQuery(qString, params);
        return getSingleResult(query);
    }

    /**
     * This method executes query and return raw results.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param <T>     Generic Type to be returned.
     * @return raw result Array.
     */
    public static <T> T getResult(String qString, Map<String, Object> params) {
        Query query = getQuery(qString, params);
        return getResultList(query);
    }

    /**
     * This method executes query and get raw results and map it to return type of method.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param <T>     Generic Type to be returned.
     * @return single Generic Type T.
     */
    public static <T> T getDynamicSingleResult(String qString, Map<String, Object> params) {
        Tuple tuple = getSingleTuple(qString, params);
        return mapper.convertValue(mapTupleToMap(tuple), new TypeReference<>() {
        });
    }

    /**
     * This method executes query and get raw results and map it to return type of method.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param <T>     Generic Type to be returned.
     * @return single Generic Type T.
     */
    public static <T> T getDynamicSingleResult(String qString, Map<String, Object> params, Class<T> tClass) {
        Tuple tuple = getSingleTuple(qString, params);
        return mapper.convertValue(mapTupleToMap(tuple), tClass);
    }


    /**
     * This method executes query and get raw results and map it to return type of method.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param <T>     Generic Type to be returned.
     * @return single Generic Type T wrapped with Optional.
     */
    public static <T> Optional<T> getWrappedDynamicSingleResult(String qString, Map<String, Object> params) {
        try {
            return Optional.of(getDynamicSingleResult(qString, params));
        } catch (NoResultException noResultException) {
            log.warn(noResultException.getMessage());
        }
        return Optional.empty();

    }

    /**
     * This method executes query and get raw results and map it to return type of method.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param <T>     Generic Type to be returned.
     * @param tClass  return Generic-type to convert
     * @return single Generic Type T wrapped with Optional.
     */
    public static <T> Optional<T> getWrappedDynamicSingleResult(String qString, Map<String, Object> params, Class<T> tClass) {
        init();
        try {
            return Optional.of(mapper.convertValue(getDynamicSingleResult(qString, params), tClass));
        } catch (NoResultException noResultException) {
            log.warn(noResultException.getMessage());
        }
        return Optional.empty();

    }

    /**
     * This method will execute query and won't return any result. Can be used for DDL queries execution.
     *
     * @param query: Native SQL Query
     */
    public static void executeUpdate(String query) {
        queryManager.createNativeQuery(query).executeUpdate();
    }

    /**
     * This method executes query and get raw results and map it to return type of method.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param <T>     Generic Type to be returned.
     * @return List of  Generic type T.
     */
    public static <T> T getDynamicResult(String qString, Map<String, Object> params) {
        List<Tuple> tuples = getTuples(qString, params);
        List<Map<String, Object>> rows = getDynamicResult(tuples);
        return mapper.convertValue(rows, new TypeReference<>() {
        });
    }

    /**
     * This method executes query and get raw results and map it to return type of method.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param <T>     Generic Type to be returned.
     * @return List of Generic type T.
     */
    public static <T> T getDynamicResult(String qString, List<Object> params) {
        List<Tuple> tuples = getTuples(qString, params);
        List<Map<String, Object>> rows = getDynamicResult(tuples);
        return mapper.convertValue(rows, new TypeReference<>() {
        });
    }

    /**
     * This method executes query and get raw results and map it to return type of method.
     *
     * @param qString native query String.
     * @param <T>     Generic Type to be returned.
     * @return List of Generic type T.
     */
    public static <T> T getDynamicResult(String qString) {
        List<Tuple> tuples = getTuples(qString);
        List<Map<String, Object>> rows = getDynamicResult(tuples);
        return mapper.convertValue(rows, new TypeReference<>() {
        });
    }

    /**
     * This method executes query and get raw results and map it to return type of method.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param <T>     Generic Type to be returned.
     * @param tClass  return Generic-type to convert
     * @return List of Generic type T.
     */
    public static <T> List<T> getDynamicResult(String qString, Map<String, Object> params, Class<T> tClass) {
        List<Tuple> tuples = getTuples(qString, params);
        return getDynamicResult(tuples, tClass);
    }


    /**
     * This method return Scrollable ResultSet use it where you want paginated data using cursors.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param tClass  return Generic-type to convert
     * @param mode    ScrollMode which can be used to scroll through queries.
     * @param <T>     Generic Type to be returned.
     * @return ScrollableResults type which can be used to scroll.
     */
    public static <T> ScrollableResults getScrollableDynamicResult(String qString, Map<String, Object> params, Class<T> tClass, ScrollMode mode) {
        return queryManager.<T>createScrollableNativeQuery(qString, tClass, params, mode);
    }

    /**
     * This method return Scrollable ResultSet use it where you want paginated data using cursors.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param tClass  return Generic-type to convert
     * @param <T>     Generic Type to be returned.
     * @return ScrollableResults type which can be used to scroll.
     */
    public static <T> ScrollableResults getScrollableDynamicResult(String qString, Map<String, Object> params, Class<T> tClass) {
        return queryManager.<T>createScrollableNativeQuery(qString, tClass, params);
    }

    /**
     * This method return Scrollable ResultSet use it where you want paginated data using cursors.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @return ScrollableResults type which can be used to scroll.
     */
    public static ScrollableResults getScrollableDynamicResult(String qString, Map<String, Object> params) {
        return queryManager.createScrollableNativeQuery(qString, params);
    }

    /**
     * This method return Scrollable ResultSet use it where you want paginated data using cursors.
     *
     * @param qString native query String.
     * @param params  query params, these params will get replaced in the query.
     * @param mode    ScrollMode which can be used to scroll through queries.
     * @return ScrollableResults type which can be used to scroll.
     */
    public static ScrollableResults getScrollableDynamicResult(String qString, Map<String, Object> params, ScrollMode mode) {
        return queryManager.createScrollableNativeQuery(qString, params, mode);
    }

    private static void init() {
        if (Objects.isNull(queryManager)) {
            synchronized (QueryExecutor.class) {
                if (Objects.isNull(queryManager)) {
                    queryManager = Instance.of(QueryManager.class);
                    mapper = getMapper();
                }
            }
        }
    }

    private static ObjectMapper getMapper() {
        return new ObjectMapper().
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
                configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .registerModule(new JavaTimeModule());
    }

    private static <T> List<Field> getFields(Class<T> tClass) {
        Field[] fields = tClass.getDeclaredFields();
        return Arrays.stream(fields).filter(field -> !field.isAnnotationPresent(QueryProcessor.class)
                ||
                !field.getAnnotation(QueryProcessor.class).ignore()).toList();
    }

    private static <T> List<T> getResultAndMapWithIndex(String qString, Map<String, Object> params, Class<T> convertTo, List<Integer> iFields) {
        Query query = getQuery(qString, params);
        List<Object> qRows = getResultList(query);
        return convertTo(qRows, convertTo, iFields);
    }

    private static <T> T getSingleResultAndMapWithIndex(String qString, Map<String, Object> params, Class<T> convertTo, List<Integer> iFields) {
        Query query = getQuery(qString, params);
        Object qRow = getSingleResult(query);
        return convertSingleRecord(qRow, iFields, convertTo);
    }

    private static List<Map<String, Object>> getResultMapWithIndex(String qString, Map<String, Object> params, List<String> fields) {
        Query query = getQuery(qString, params);
        List<Object> qRows = getResultList(query);
        return mapper.convertValue(convert(qRows, LinkedHashMap.class, fields), new TypeReference<>() {
        });

    }

    private static Map<String, Object> getSingleResultMapWithIndex(String qString, Map<String, Object> params, List<String> fields) {
        Query query = getQuery(qString, params);
        Object qRow = getSingleResult(query);
        return mapper.convertValue(convertSingleRecord(qRow, fields, LinkedHashMap.class), new TypeReference<>() {
        });

    }

    private static String getFieldName(Field field) {
        return field.isAnnotationPresent(JsonProperty.class) ? field.getAnnotation(JsonProperty.class).value() : field.getName();
    }

    private static int getFieldIndex(Field field) {
        if (field.isAnnotationPresent(JsonProperty.class)) {
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            if (jsonProperty.index() >= 0)
                return jsonProperty.index();
        }
        throw new RowIndexMandatory();
    }

    private static <T> Query getQuery(String qString, Map<String, Object> params, Class<T> convertTo) {
        init();
        Query query = queryManager.createNativeQuery(qString, convertTo);
        if (Objects.nonNull(params)) {
            params.forEach(query::setParameter);
        }
        return query;
    }

    private static <T, R> List<T> convertTo(List<R> qRows, RowMapper<T, R> rowMapper) {
        List<T> rows = new LinkedList<>();
        for (R row : qRows) {
            rows.add(convertTo(row, rowMapper));
        }
        return rows;
    }

    private static <T> List<T> convertTo(List<Object> qRows, Class<T> convertTo, List<Integer> iFields) {
        return convert(qRows, convertTo, iFields);

    }

    private static <T> List<T> convert(List<Object> qRows, Class<T> convertTo, List<?> iFields) {
        List<T> rows = new LinkedList<>();
        for (Object arrayRow : qRows) {
            rows.add(convertSingleRecord(arrayRow, iFields, convertTo));
        }
        return rows;

    }

    private static <T> T convertSingleRecord(Object qRow, List<?> iFields, Class<T> convertTo) {
        Map<String, Object> rowMap = new LinkedHashMap<>();
        if (qRow instanceof Object[] array) {
            for (int index = 0; index < iFields.size(); index++) {
                rowMap.put(keyName(iFields.get(index)), array[index]);
            }
        } else {
            rowMap.put(keyName(iFields.get(0)), qRow);
        }
        return mapper.convertValue(rowMap, convertTo);

    }

    private static String keyName(Object obj) {
        return obj instanceof Integer key ? NumberEnum.getValue(key).name() : String.valueOf(obj);
    }

    private static <T, R> T convertTo(R row, RowMapper<T, R> rowMapper) {
        return rowMapper.get(row);
    }

    private static Query getQuery(String qString, Map<String, Object> params) {
        init();
        Query query = queryManager.createNativeQuery(qString);
        if (Objects.nonNull(params)) {
            params.forEach(query::setParameter);
        }
        return query;
    }

    private static Query getQuery(String qString, List<Object> params) {
        init();
        Query query = queryManager.createNativeQuery(qString);
        AtomicInteger pos = new AtomicInteger(1);
        if (Objects.nonNull(params)) {
            params.forEach(param -> query.setParameter(pos.getAndIncrement(), param));
        }
        return query;
    }

    private static Query getQueryWithTuple(String qString, Map<String, Object> params) {
        init();
        Query query = queryManager.createNativeQuery(qString, Tuple.class);
        if (Objects.nonNull(params)) {
            params.forEach(query::setParameter);
        }
        return query;
    }

    private static Query getQueryWithTuple(String qString, List<Object> params) {
        init();
        Query query = queryManager.createNativeQuery(qString, Tuple.class);
        AtomicInteger pos = new AtomicInteger(1);
        if (params != null) {
            params.forEach(param -> query.setParameter(pos.getAndIncrement(), param));
        }
        return query;
    }

    private static Query getQueryWithTuple(String qString) {
        init();
        return queryManager.createNativeQuery(qString, Tuple.class);

    }

    private static List<LinkedHashMap<String, Object>> getResultMap(String qString, Map<String, Object> params, List<String> fields) {
        List<Tuple> tuples = getTuples(qString, params);
        List<LinkedHashMap<String, Object>> tRows = new LinkedList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> row = mapTupleToMap(tuple, fields);
            tRows.add(mapper.convertValue(row, new TypeReference<>() {
            }));
        }
        return tRows;

    }

    public static List<Tuple> getTuples(String qString, Map<String, Object> params) {
        Query query = getQueryWithTuple(qString, params);
        return getResultList(query);
    }

    public static List<Tuple> getTuples(String qString, List<Object> params) {
        Query query = getQueryWithTuple(qString, params);
        return getResultList(query);
    }

    public static List<Tuple> getTuples(String qString) {
        Query query = getQueryWithTuple(qString);
        return getResultList(query);
    }

    public static NativeQuery<Tuple> getQueryWithScroll(String qString, Map<String, Object> params) {
        init();
        NativeQuery<Tuple> query = queryManager.createNativeQueryWithScroll(qString);
        if (Objects.nonNull(params)) {
            params.forEach(query::setParameter);
        }
        return query;
    }


    private static Tuple getSingleTuple(String qString, Map<String, Object> params) {
        Query query = getQueryWithTuple(qString, params);
        return getSingleResult(query);
    }

    private static Map<String, Object> mapTupleToMap(Tuple tuple, List<String> fields) {
        Map<String, Object> row = new LinkedHashMap<>();
        fields.forEach(field -> row.put(field, getValue(tuple, field, queryManager.getKey(field))));
        return row;
    }

    private static Map<String, Object> mapTupleToMap(Tuple tuple) {
        Map<String, Object> row = new LinkedHashMap<>();
        tuple.getElements().forEach(tupleElement -> {
            String key = queryManager.getKey(tupleElement.getAlias());
            row.put(key, tuple.get(tupleElement));
        });
        return row;
    }

    private static <T> List<T> getDynamicResult(List<Tuple> tuples, Class<T> tClass) {
        List<T> rows = new LinkedList<>();
        for (Tuple tuple : tuples) {
            rows.add(mapper.convertValue(mapTupleToMap(tuple), tClass));
        }
        return rows;
    }

    private static List<Map<String, Object>> getDynamicResult(List<Tuple> tuples) {
        List<Map<String, Object>> rows = new LinkedList<>();
        for (Tuple tuple : tuples) {
            rows.add(mapTupleToMap(tuple));
        }
        return rows;
    }

    private static Object getValue(Tuple tuple, String... keys) {
        for (String key : keys) {
            Object value = null;
            try {
                value = tuple.get(key);
            } catch (IllegalArgumentException e) {
                log.warn("Illegal Argument {}", key);
            }

            if (Objects.nonNull(value)) {
                return value;
            }
        }
        return null;
    }

    private static <R> R getResultList(Query query) {
        return (R) query.getResultList();
    }

    private static <R> R getSingleResult(Query query) {
        return (R) query.getSingleResult();
    }

    private static <T> List<T> getResultObject(String qString, Map<String, Object> params, Class<T> convertTo, List<String> fields) {
        List<Tuple> tuples = getTuples(qString, params);
        List<T> tRows = new LinkedList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> rows = mapTupleToMap(tuple, fields);
            tRows.add(mapper.convertValue(rows, convertTo));
        }
        return tRows;

    }

    private static <T> T getSingleResultObject(String qString, Map<String, Object> params, Class<T> convertTo, List<String> fields) {
        Tuple tuple = getSingleTuple(qString, params);
        Map<String, Object> rows = mapTupleToMap(tuple, fields);
        return mapper.convertValue(rows, convertTo);
    }
}
