package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.spring.SpringAppContext;
import com.github.bingoohuang.westcache.utils.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/16.
 */
@Slf4j
public class QuartzCacheFlusher extends ByPassCacheFlusher {
    Cache<String, Pair<WestCacheOption, WestCache>>
            registry = CacheBuilder.newBuilder().build();
    Quartz quartz = new Quartz();

    @Override
    public boolean register(final WestCacheOption option,
                            final String cacheKey,
                            final WestCache cache) {
        val scheduled = getScheduled(option);
        if (StringUtils.isBlank(scheduled)) return false;

        Pair<WestCacheOption, WestCache> pair;
        pair = registry.getIfPresent(cacheKey);
        if (pair != null) return false;

        Guavas.cacheGet(registry, cacheKey, new Callable<Pair<WestCacheOption, WestCache>>() {
            @Override
            public Pair<WestCacheOption, WestCache> call() throws Exception {
                val job = JobBuilder.newJob(RunnableCacheJob.class).build();
                job.getJobDataMap().put(RunnableCacheJob.KEY, new Runnable() {
                    @Override
                    public void run() {
                        for (val entry : registry.asMap().entrySet()) {
                            String key = entry.getKey();
                            WestCache cac = entry.getValue().getValue();
                            WestCacheOption opt = entry.getValue().getLeft();
                            cac.invalidate(opt, key, null);

                            log.debug("cache invalidate key {}", key);
                        }
                    }
                });

                val trigger = new ScheduledParser(scheduled).parse();
                quartz.scheduleJob(job, trigger);
                return Pair.of(option, cache);
            }
        });

        return true;
    }

    private String getScheduled(WestCacheOption option) {
        String scheduled = option.getSpecs().get("scheduled");
        if (StringUtils.isNotBlank(scheduled)) return scheduled;

        String scheduledBean = option.getSpecs().get("scheduledBean");
        if (Envs.HAS_SPRING && StringUtils.isNotBlank(scheduledBean)) {
            return SpringAppContext.getBean(scheduledBean);
        }

        return null;
    }

    public static class RunnableCacheJob implements Job {
        public static final String KEY = "runnable";

        @Override
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
            val jobDataMap = context.getJobDetail().getJobDataMap();
            val runnable = (Runnable) jobDataMap.get(KEY);
            runnable.run();
        }
    }

    public void stopQuartz() {
        quartz.stop();
    }
}
