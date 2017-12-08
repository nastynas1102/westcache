package com.github.bingoohuang.westcache.outofbox;

import com.alibaba.fastjson.TypeReference;
import com.github.bingoohuang.westcache.flusher.DirectValueType;
import com.github.bingoohuang.westcache.flusher.TableBasedCacheFlusher;
import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.mybatis.MybatisFactory;
import com.github.bingoohuang.westcache.mybatis.WestCacheMapper;
import com.github.bingoohuang.westcache.utils.*;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.n3r.eql.eqler.EqlerFactory;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/30.
 */
public class TableCacheFlusher extends TableBasedCacheFlusher {
    @Getter
    SqlSessionFactory sqlSessionFactory = Guavas.cacheGet(MybatisFactory.sessionFactoryCache, this.getClass());
    @Getter
    WestCacheMapper dao = MybatisFactory.proxyMybatisDao(sqlSessionFactory);

    @Override
    protected List<WestCacheFlusherBean> queryAllBeans() {
        return dao.selectAllBeans();
    }

    @Override
    protected Object readDirectValue(WestCacheOption option,
                                     WestCacheFlusherBean bean,
                                     DirectValueType type) {
        val specs = Specs.parseSpecs(bean.getSpecs());
        val readBy = specs.get("readBy");
        val value = readByRedis(option, bean, readBy);
        if (value != null) return value;

        val loader = readByLoader(specs, readBy);
        if (loader != null) return loader;

        return readByDirect(option, bean, type);
    }

    private Object readByDirect(WestCacheOption option,
                                WestCacheFlusherBean bean,
                                DirectValueType type) {
        val directJson = dao.getDirectValue(bean.getCacheKey());
        if (StringUtils.isBlank(directJson)) return null;

        switch (type) {
            case FULL:
                return FastJsons.parse(directJson, option.getMethod(), true);
            case SUB:
            default:
                val typeReference = new TypeReference<Map<String, String>>() {
                };
                return FastJsons.parse(directJson, typeReference);
        }
    }

    private Object readByLoader(Map<String, String> specs, String readBy) {
        if (!"loader".equals(readBy)) return null;

        val loaderClass = specs.get("loaderClass");
        if (StringUtils.isBlank(loaderClass)) return null;

        Callable loader = Envs.newInstance(loaderClass);
        return Envs.execute(loader);
    }

    private Object readByRedis(WestCacheOption option,
                               WestCacheFlusherBean bean,
                               String readBy) {
        if (!"redis".equals(readBy)) return null;

        val key = Redis.PREFIX + bean.getCacheKey();
        val value = Redis.getRedis(option).get(key);
        if (StringUtils.isBlank(value)) return null;

        return FastJsons.parse(value, option.getMethod(), true);
    }

}
