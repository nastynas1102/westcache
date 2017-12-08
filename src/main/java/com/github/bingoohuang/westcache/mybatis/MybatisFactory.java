package com.github.bingoohuang.westcache.mybatis;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by  NastyNas on 2017/12/7.
 */
public class MybatisFactory {

    public static LoadingCache<Class, SqlSessionFactory> sessionFactoryCache = CacheBuilder.newBuilder().build(new CacheLoader<Class, SqlSessionFactory>() {
        public SqlSessionFactory load(Class clazz) throws Exception {
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://192.168.0.111:3306/fjjh?allowMultiQueries=true&tinyInt1isBit=false&useSSL=false");
            dataSource.setUsername("lshd123");
            dataSource.setPassword("lshd123");
            dataSource.setInitialSize(5);
            dataSource.setMaxActive(10);
            dataSource.setDefaultAutoCommit(true);
            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment("development", transactionFactory, dataSource);
            Configuration configuration = new Configuration(environment);
            configuration.addMapper(WestCacheMapper.class);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
            return sqlSessionFactory;
        }
    });

    public static WestCacheMapper proxyMybatisDao(SqlSessionFactory sqlSessionFactory) {
        return (WestCacheMapper) Proxy.newProxyInstance(
                SqlSessionFactory.class.getClassLoader(),
                new Class[]{WestCacheMapper.class},
                new MybatisInvocationHandler(sqlSessionFactory));
    }

    public static class MybatisInvocationHandler implements InvocationHandler {
        public MybatisInvocationHandler(SqlSessionFactory sqlSessionFactory) {
            this.sqlSessionFactory = sqlSessionFactory;
        }

        final SqlSessionFactory sqlSessionFactory;

        @Override
        public Object invoke(Object proxy,
                             Method method,
                             Object[] args) throws Throwable {
            SqlSession session = null;
            WestCacheMapper dao = null;
            try {
                session = sqlSessionFactory.openSession(true);
                dao = session.getMapper(WestCacheMapper.class);
                Object result = method.invoke(dao, args);
                return result;
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
    }

    public MybatisFactory() {
    }
}
