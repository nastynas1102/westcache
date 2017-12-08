package com.github.bingoohuang.westcache.mybatis;

import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by  NastyNas on 2017/12/7.
 */
public interface WestCacheMapper {

    @Select("SELECT CACHE_KEY cacheKey, KEY_MATCH keyMatch,  VALUE_VERSION valueVersion , VALUE_TYPE valueType, SPECS specs " +
            "FROM WESTCACHE_FLUSHER " +
            "WHERE CACHE_STATE = 1 " +
            "ORDER BY MATCH_PRI DESC")
    @ResultType(value = WestCacheFlusherBean.class)
    List<WestCacheFlusherBean> selectAllBeans();

    @Select("SELECT DIRECT_VALUE FROM WESTCACHE_FLUSHER " +
            "WHERE CACHE_KEY = #{key} AND CACHE_STATE = 1")
    String getDirectValue(@Param("key") String key);

    @Insert("INSERT INTO WESTCACHE_FLUSHER(CACHE_KEY, KEY_MATCH, VALUE_VERSION, VALUE_TYPE, SPECS) " +
            "VALUES(#{cacheKey}, #{keyMatch}, #{valueVersion}, #{valueType}, #{specs})")
    Integer addBean(WestCacheFlusherBean bean);

    @Update("UPDATE WESTCACHE_FLUSHER " +
            "SET CACHE_STATE = 0 " +
            "WHERE CACHE_KEY = #{cacheKey}" +
            "AND CACHE_STATE <> 0")
    Integer disableBean(WestCacheFlusherBean bean);

    @Update("UPDATE WESTCACHE_FLUSHER SET VALUE_VERSION = VALUE_VERSION + 1," +
            "DIRECT_VALUE = #{directValue} " +
            "WHERE CACHE_KEY = #{cacheKey}")
    Integer updateDirectValue(@Param("cacheKey") String cacheKey, @Param("directValue") String directValue);

    @Update("UPDATE WESTCACHE_FLUSHER SET VALUE_VERSION = VALUE_VERSION + 1 " +
            "WHERE CACHE_KEY = #{key}")
    Integer upgradeVersion(@Param("key") String key);
}
