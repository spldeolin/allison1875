package __NAMESPACE__.id;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import __NAMESPACE__.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SnowFlake {

    private static final long OFFSET = TimeUtils.toUnixTimestamp(LocalDateTime.of(2023, 4, 13, 10, 0, 0));

    private static final long WORKER_ID_BITS = 5L;

    private static final long SEQUENCE_ID_BITS = 16L;

    private static final long WORKER_SHIFT_BITS = SEQUENCE_ID_BITS;

    private static final long OFFSET_SHIFT_BITS = SEQUENCE_ID_BITS + WORKER_ID_BITS;

    private static final long BACK_WORKER_ID_BEGIN = (1 << WORKER_ID_BITS) >> 1;

    private static final long SEQUENCE_MAX = (1 << SEQUENCE_ID_BITS) - 1;

    private static final long BACK_TIME_MAX = 1L;

    private static long lastTimestamp = 0L;

    private static long sequence = 0L;

    private static long lastTimestampBak = 0L;

    private static long sequenceBak = 0L;

    @Value("${snow-flake.machine-id}")
    private Long workerId;

    @Autowired
    private RedissonClient redissonClient;

    @Value("${spring.application.name}")
    private String applicationName;

    @PostConstruct
    public void init() {
        RLock lock = redissonClient.getLock(applicationName + ":lock:snowflake");
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                RBucket<Integer> bucket = redissonClient.getBucket(applicationName + ":counter:snowflake");
                Integer counter = bucket.get();
                if (counter == null || counter > 31) {
                    counter = 0;
                }
                workerId = (long) counter;
                bucket.set(counter + 1);
            } else {
                throw new RuntimeException("failed to init SnowFlake");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("failed to init SnowFlake");
        } finally {
            lock.unlock();
        }
        log.info("SnowFlake inited, workerId={}", workerId);
    }

    public long nextId() {
        return nextId(System.currentTimeMillis() / 1000);
    }

    private synchronized long nextId(long timestamp) {
        if (timestamp < lastTimestamp) {
            return nextIdBackup(timestamp);
        }
        if (timestamp != lastTimestamp) {
            lastTimestamp = timestamp;
            sequence = 0L;
        }
        if (0L == (++sequence & SEQUENCE_MAX)) {
            sequence--;
            return nextIdBackup(Math.max(timestamp, lastTimestampBak));
        }
        return ((timestamp - OFFSET) << OFFSET_SHIFT_BITS) | (workerId << WORKER_SHIFT_BITS) | sequence;
    }

    private long nextIdBackup(long timestamp) {
        if (timestamp < lastTimestampBak) {
            if (lastTimestampBak - (System.currentTimeMillis() / 1000) <= BACK_TIME_MAX) {
                timestamp = lastTimestampBak;
            } else {
                throw new RuntimeException(
                        String.format("时钟回拨: now: [%d] last: [%d]", timestamp, lastTimestampBak));
            }
        }
        if (timestamp != lastTimestampBak) {
            lastTimestampBak = timestamp;
            sequenceBak = 0L;
        }
        if (0L == (++sequenceBak & SEQUENCE_MAX)) {
            return nextIdBackup(timestamp + 1);
        }
        return ((timestamp - OFFSET) << OFFSET_SHIFT_BITS) | ((workerId ^ BACK_WORKER_ID_BEGIN) << WORKER_SHIFT_BITS)
                | sequenceBak;
    }

    public Long getWorkerId() {
        return workerId;
    }

}
