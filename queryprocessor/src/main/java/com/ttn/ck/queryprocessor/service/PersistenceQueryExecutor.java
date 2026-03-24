package com.ttn.ck.queryprocessor.service;

import com.ttn.ck.queryprocessor.utils.Instance;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Saurabh Aggarwal
 * Query Executor framework is based on Entity manager bean, and it provides utilty functions/methods to fetch data and map data to respective DTO's.
 * Query Executor is used to process native queries, to process namedQueries , You can use original entities na repos to bind them.
 * This class is thread safe.
 */
@Slf4j
public class PersistenceQueryExecutor {
    private static QueryManager queryManager;

    private PersistenceQueryExecutor() {
    }

    /***
     * This method will be used to insert record in DB
     * @param qString the query sent from calling
     * @param params the dynamic parameters
     * @return the number of records updated
     */
    public static int save(String qString, Map<String, Object> params) {
        Query query = getQuery(qString, params);
        return executeUpdate(query);
    }

    private static void init() {
        if (Objects.isNull(queryManager)) {
            synchronized (PersistenceQueryExecutor.class) {
                if (Objects.isNull(queryManager)) {
                    queryManager = Instance.of(QueryManager.class);
                }
            }
        }
    }

    private static Query getQuery(String qString, Map<String, Object> params) {
        init();
        Query query = queryManager.createNativeQuery(qString);
        if (Objects.nonNull(params)) {
            params.forEach(query::setParameter);
        }
        return query;
    }

    private static int executeUpdate(Query query) {
        Transaction transaction = createSessionAndBeginTransaction();
        int result = query.executeUpdate();
        transaction.commit();
        return result;
    }

    private static Transaction createSessionAndBeginTransaction() {
        Session session = queryManager.getSession();
        return session.beginTransaction();
    }

    /**
     * This method is used to Bulk update in Snowflake
     * The List of Map received in request body, should contain values to be stored in DB.
     * The map key will be the sequence of values as used in query starting with 1.
     *
     * @param qString the query sent from the calling method
     * @param params  the parameter with their respective index
     */
    public static void saveAll(String qString, List<Map<Integer, Object>> params) {
        init();
        Session session = queryManager.getSession();
        session.doWork(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(qString)) {
                int i = 1;
                for (Map<Integer, Object> objMap : params) {
                    for (Map.Entry<Integer, Object> entry : objMap.entrySet()) {
                        preparedStatement.setObject(entry.getKey(), entry.getValue());
                    }
                    preparedStatement.addBatch();
                    if (i % 20 == 0) {
                        preparedStatement.executeBatch();
                    }
                    i++;
                }
                preparedStatement.executeBatch();
            } catch (SQLException e) {
                log.error("An exception occurred in SampleNativeQueryRepository.bulkInsertName: ", e);
            }
        });
    }

}
