/*
 * Copyright 2025-2030 大连云建数科科技有限公司.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yunjian.datarelation.pool;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.yunjian.datarelation.common.BaseOptions;
import com.yunjian.datarelation.config.DataRelationAutoConfiguration;
import com.yunjian.datarelation.config.ReportProperties;
import com.yunjian.datarelation.dto.DatasourceDTO;
import com.yunjian.datarelation.dto.DatasourcePageDTO;
import com.yunjian.datarelation.dto.TableDTO;
import com.yunjian.datarelation.entity.Datasource;
import com.yunjian.datarelation.event.DatasourceChangeEvent;
import com.yunjian.datarelation.service.DatasourceService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 数据源池管理器类，数据源的管理由程序自行管理，自动管理数据源池的生命周期，不需要手动管理。
 *
 * @author yujian
 **/
@Slf4j
@RequiredArgsConstructor
public class DatasourcePoolManager implements ApplicationListener<DatasourceChangeEvent>,
        DisposableBean {

    /**
     * 是否需要处理逻辑删除上下文
     */
    public static final ThreadLocal<LogicDelete> LOGIC_DELETE_HOLDER = new TransmittableThreadLocal<>();
    /**
     * 健康检查线程池
     */
    private final ScheduledThreadPoolExecutor healthCheckExecutor;
    /**
     * 数据源池化管理存储
     */
    private final Map<Long, Ds> dsMap = new ConcurrentHashMap<>();
    /**
     * 操作数据源时需先获取锁(公平锁)
     */
    private final ReentrantLock lock = new ReentrantLock(true);
    /**
     * 持有锁的最大时间（秒）
     */
    private final int lockTimeOut = 3;
    /**
     * 是否已经初始化
     */
    private final AtomicBoolean init = new AtomicBoolean(false);
    /**
     * 数据源配置信息 在nacos中读取
     *
     * @see DataRelationAutoConfiguration#reportProperties() ()
     */
    private final ReportProperties.DatasourceProperties datasourcePoolProperties;
    /**
     * 序列化缓存
     *
     * @see DataRelationAutoConfiguration#redisTemplate(RedisConnectionFactory)
     */
    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 可以填充逻辑删除字段表的二级缓存
     */
    private final Map<String, List<String>> logicDeleteTables = new HashMap<>();
    /**
     * 逻辑删除缓存key
     */
    private static final String LOGIC_DELETE_KEY = "LogicDeleteTables";

    private DatasourceService datasourceService;

    private ThreadPoolTaskExecutor springExecutor;

    /**
     * 初始化需要依赖DatasourceService,构造器初始化bean会认为循环依赖，所以在调用方初始化
     *
     * @param bean DatasourceService
     */
    public void init(DatasourceService bean) {
        if (init.compareAndSet(false, true)) {
            datasourceService = bean;
            List<DatasourceDTO> records = bean.getList(new DatasourcePageDTO(), true);
            log.info("初始化数据源健康检查Task.");
            initHealthCheckTask();
            log.info("初始化用户自定义Datasource连接池.");
            initDatasourcePool(records);
            log.info("初始化扫描逻辑删除字段表.");
            initScanLogicDeleteTable(records);
        }
    }


    /**
     * 根据数据源ID获取数据源信息。
     * <p>
     * 该方法有取不到数据源的时候，原因可能是在程序启动时无法连接已经存在的数据源,此时返回空的Optional
     * <p>
     * 如果是初始化时没有问题，被健康检查发现无法连接，则正常返回Ds但是healthStatus是false, 如果此时没有检查healthStatus继续使用的话会导致后面执行sql时报错
     *
     * @param datasourceId 数据源的唯一标识ID。这个参数用于在数据源列表中定位特定的数据源。
     * @return 返回一个Optional对象，其中包含所请求的数据源对象。如果找不到匹配的数据源，则该数据源无法访问或已经过期等。
     */
    public Optional<Ds> getDatasource(Long datasourceId) {
        try {
            if (lock.tryLock(lockTimeOut, TimeUnit.SECONDS)) {
                return Optional.ofNullable(dsMap.get(datasourceId));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取数据源锁时被中断", e);
        } finally {
            lock.unlock();
        }
        // old datasource
        return Optional.ofNullable(dsMap.get(datasourceId));
    }

    /**
     * 添加数据源信息。
     * <p>
     * 本方法用于将一个新的数据源实例添加到系统中。通过传入的数据源ID和数据源实例， 对数据源进行相应的管理和配置。此方法在系统初始化或动态添加数据源时被调用。
     * </p>
     *
     * @param datasourceId 数据源的唯一标识ID。用于在系统中唯一标识一个数据源。
     * @param datasource   数据源实例对象。包含了数据源的连接配置信息等。
     */
    public void addDatasource(Long datasourceId, Datasource datasource) {

        try {
            if (lock.tryLock(lockTimeOut, TimeUnit.SECONDS)) {
                dsMap.computeIfAbsent(datasourceId,
                        key -> new Ds(datasource, datasourcePoolProperties));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取数据源锁时被中断", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 移除数据源。
     * <p>
     * 该方法用于从系统中移除指定的数据源。通过数据源的ID定位到具体的数据源实例，并执行移除操作。 移除数据源可能涉及数据库连接的释放、相关配置的清除等工作，确保系统中不再存在对该数据源的引用。
     *
     * @param datasourceId 数据源的唯一标识ID。用于定位要移除的具体数据源实例。
     */
    private void removeDatasource(Long datasourceId) {
        try {
            if (lock.tryLock(lockTimeOut, TimeUnit.SECONDS)) {
                getDatasource(datasourceId)
                        .ifPresent(ds -> {
                            log.info("关闭数据源连接 {}", ds.getInstance().getName());
                            ds.getDataSource().close();
                            dsMap.remove(datasourceId);
                        });
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取数据源锁时被中断", e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检查状态
     *
     * @param datasourceId 数据源id
     * @param test         是否进行连接检查
     */
    public boolean health(Long datasourceId, boolean test, Supplier<Boolean> notExist) {
        Optional<Ds> datasource = getDatasource(datasourceId);
        if (datasource.isPresent()) {
            Ds ds = datasource.get();
            boolean testNow = true;
            if (test) {
                testNow = ds.getOptions().testConnection();
                ds.changeStatus.accept(testNow);
            }
            return testNow;
        }
        return notExist.get();
    }

    /**
     * 只适配了单点，如果多实例报表需要对该方法改造通知其他实例
     * <p>
     *
     * @param event 数据源curd事件
     */
    @Override
    public void onApplicationEvent(@Nonnull DatasourceChangeEvent event) {
        lock.lock();
        try {
            switch (event.getEventEnum()) {
                case ADD: {
                    log.info("监听到【新增数据源】事件,池化数据源 {}",
                            JSONUtil.toJsonPrettyStr(event.getInstance()));
                    addDatasource(event.getDatasourceId(), event.getInstance());
                    // 将新的数据源对应的字段扫描
                    doScan(event.getInstance().convert())
                            .thenAccept(nope -> flushLogicDeleteCache());
                    break;
                }
                case REMOVE: {
                    log.info("监听到【删除数据源】事件 id = {}", event.getDatasourceId());
                    removeDatasource(event.getDatasourceId());
                    break;
                }
                case UPDATE: {
                    log.info("监听到【更新数据源】事件,Reset数据源 {}",
                            JSONUtil.toJsonPrettyStr(event.getInstance()));
                    removeDatasource(event.getDatasourceId());
                    addDatasource(event.getDatasourceId(), event.getInstance());
                    doScan(event.getInstance().convert())
                            .thenAccept(nope -> flushLogicDeleteCache());
                    break;
                }
                default:
                    break;
            }
        } finally {
            lock.unlock();
        }


    }

    /**
     * 销毁当前对象或资源的方法。
     * <p>
     * 该方法用于释放由当前对象管理的资源或进行清理工作，以确保在对象不再使用时，不会造成内存泄漏或其他资源占用问题。 具体的销毁行为取决于实现，可能包括关闭连接、释放内存、取消任务等。
     * <p>
     * 注意：该方法没有参数和返回值。
     */
    @Override
    public void destroy() {
        healthCheckExecutor.shutdown();
        dsMap.forEach((k, v) -> {
            log.info("关闭用户自定义数据源 {}", v.getInstance().getName());
            v.getDataSource().close();
        });
    }

    /**
     * 检查数据源可用性,可用时恢复
     */
    private void initHealthCheckTask() {
        final List<Long> badList = new ArrayList<>();

        Integer period = Optional.ofNullable(datasourcePoolProperties.getHealthCheckTimeMillis())
                .orElse(5000);

        healthCheckExecutor.scheduleAtFixedRate(() -> {
            log.debug("数据源健康检查开始..");
            if (!lock.isLocked()) {
                List<DatasourceDTO> list = datasourceService.getList(new DatasourcePageDTO(), true);
                for (DatasourceDTO record : list) {
                    boolean health = health(record.getId(), true, () -> {
                        // 启动时无法连接,检查如果恢复了就自动加入连接池
                        boolean testOk = record.getOptions().testConnection();
                        if (testOk) {
                            addDatasource(record.getId(), record.convert());
                            return true;
                        }
                        return false;
                    });
                    if (!health) {
                        badList.add(record.getId());
                    }
                }
                if (CollUtil.isNotEmpty(badList)) {
                    log.warn("数据源健康检查发现不可用数据源Id = {}", JSONUtil.toJsonPrettyStr(badList));
                    badList.clear();
                }
            }
        }, 50000, period < 1000 ? 1000 : period, TimeUnit.MILLISECONDS);
    }

    /**
     * 池化所有库中数据源
     *
     * @param records 数据源集合
     */
    private void initDatasourcePool(List<DatasourceDTO> records) {
        // 不阻塞main线程,但是程序启动完成后可能会出现部分功能不好用的情况，是因为连接池还未初始化完成（特殊情况）
        ThreadUtil.newSingleExecutor()
                .execute(() -> {
                    for (DatasourceDTO record : records) {
                        Datasource ds = record.convert();
                        // 这里的启动测试连接不是必须的,除了手动改库一般不会造成已经创建的数据源有连接问题
                        // 主要防止切换环境导致内网ip变化或云数据库过期等情况
                        // 可以添加一个参数来决定是否再启动时测试连接，加快启动速度
                        if (!ds.getOptions().testConnection()) {
                            continue;
                        }
                        addDatasource(record.getId(), ds);
                    }
                });
    }

    /**
     * 如果该数据源配置了逻辑删除字段,那么扫描所有数据源下的表，将含有逻辑删除字段的表缓存
     */
    public void initScanLogicDeleteTable(List<DatasourceDTO> list) {
        springExecutor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
        springExecutor.execute(() -> {
            flushLogicDeleteCache();

            redisTemplate.delete(LOGIC_DELETE_KEY);
            List<CompletableFuture<Void>> allFuture = new ArrayList<>();
            // 所有数据源
            for (DatasourceDTO record : list) {
                allFuture.add(doScan(record));
            }
            CompletableFuture.allOf(allFuture.toArray(new CompletableFuture[0])).join();

            flushLogicDeleteCache();

        });
    }

    /**
     * 刷新到二级缓存
     */
    @SuppressWarnings("unchecked")
    public void flushLogicDeleteCache() {
        logicDeleteTables.clear();
        Set<Object> datasourceIds = redisTemplate.opsForHash().keys(LOGIC_DELETE_KEY);
        for (Object datasourceId : datasourceIds) {
            List<String> list = (List<String>) redisTemplate.opsForHash()
                    .get(LOGIC_DELETE_KEY, datasourceId);
            logicDeleteTables.put((String) datasourceId, list);
        }
    }

    /**
     * 查看当前数据源中,该表是否含有逻辑删除字段
     *
     * @param datasourceId 数据源id
     * @param tableName    表名
     * @return 是否含有
     */
    public boolean isLogicDeleteTable(Long datasourceId, String tableName) {
        return logicDeleteTables.getOrDefault(String.valueOf(datasourceId), Collections.emptyList())
                .contains(tableName);
    }

    public CompletableFuture<Void> doScan(DatasourceDTO record) {
        final List<String> haveTableNames = logicDeleteTables.getOrDefault(
                String.valueOf(record.getId()),
                new ArrayList<>());
        return CompletableFuture.runAsync(
                () -> getDatasource(record.getId()).ifPresent(ds -> {

                    BaseOptions options = ds.getOptions();
                    // 是否配置了逻辑删除
                    if (StrUtil.isAllNotBlank(options.getLogicDeleteField(), options
                            .getLogicDeleteValue())) {
                        // 所有表
                        List<String> hasLogicFieldTables = record.getOptions().allTables(null)
                                .stream()
                                .map(TableDTO::getName)
                                // 每次启动时,扫描哪些未含有逻辑删除字段的表,本次是否已经加上了逻辑删除字段
                                .filter(name -> !haveTableNames.contains(name))
                                // once query
                                .filter(name -> record.getOptions()
                                        .tableFields(name).stream()
                                        .anyMatch(
                                                field -> field.getName()
                                                        .equalsIgnoreCase(options.getLogicDeleteField())))
                                .peek(name -> log.info("新增逻辑删除表 {}", name))
                                .collect(Collectors.toList());

                        List<String> old = logicDeleteTables.computeIfAbsent(String.valueOf(record.getId()),
                                k -> new ArrayList<>());
                        old.addAll(hasLogicFieldTables);

                        redisTemplate.opsForHash()
                                .put(LOGIC_DELETE_KEY, String.valueOf(record.getId()), old);
                    }
                }), springExecutor);
    }

}
