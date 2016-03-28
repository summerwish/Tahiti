package octoteam.tahiti.shared.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 父日志对象,
 * 初始化一个日志对象, 根据日志输出路径创建输出器 Appender 并对其进行日志输出模式 Pattern 的配置,
 * 并按给定时间间隔定时执行日志记录.
 */
public abstract class StatisticsLogger {

    private final Logger logger = (Logger) LoggerFactory.getLogger(StatisticsLogger.class);

    private HashMap<String, AtomicInteger> counters = new HashMap<>();

    private void initLogger(String filePath) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("File");
        fileAppender.setFile(filePath);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setImmediateFlush(true);
        encoder.setPattern("%d %-5level - %msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        logger.setLevel(Level.ALL);
        logger.addAppender(fileAppender);
    }

    private void initScheduler(int periodSeconds) {
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(this::logStatistics, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    private void logStatistics() {
        counters.forEach((key, value) -> logger.info(String.format("Statistics: %s: %d", key, value.get())));
        logger.info("------------------");
    }

    /**
     * 构造函数 按给定路径初始化日志对象,并按给定时间间隔定时执行.
     *
     * @param filePath      日志的存储路径
     * @param periodSeconds 日志执行记录的时间周期,单位为秒
     */
    public StatisticsLogger(String filePath, int periodSeconds) {
        initLogger(filePath);
        initScheduler(periodSeconds);
    }

    /**
     * 通过输入新键值对,初始化需要记录的事件信息,时间对应初始次数值为0.
     *
     * @param key 要记录的事件名称
     */
    protected void clear(String key) {
        counters.put(key, new AtomicInteger());
    }

    /**
     * 对相应记录事件的次数值进行加1
     *
     * @param key 要增加发生次数的事件名称
     * @return 事件目前发生的总次数
     */
    protected int increase(String key) {
        return counters.get(key).incrementAndGet();
    }

}
