import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AdvancedCache<K, V> {
    private final ConcurrentHashMap<K, CacheEntry<V>> cache;
    private final LinkedHashMap<K, Long> accessOrder;
    private final ReentrantReadWriteLock lock;
    private final long defaultExpiry;
    private final int maxSize;
    private final ScheduledExecutorService cleaner;

    public AdvancedCache(int maxSize, long defaultExpiryMillis) {
        this.maxSize = maxSize;
        this.defaultExpiry = defaultExpiryMillis;
        this.cache = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.accessOrder = new LinkedHashMap<>(16, 0.75f, true);
        
        this.cleaner = Executors.newScheduledThreadPool(1);
        this.cleaner.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    private static class CacheEntry<V> {
        private final V value;
        private final long expiryTime;
        private final long creationTime;

        public CacheEntry(V value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
            this.creationTime = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    public void put(K key, V value) {
        put(key, value, defaultExpiry);
    }

    public void put(K key, V value, long expiryMillis) {
        lock.writeLock().lock();
        try {
            if (cache.size() >= maxSize) {
                evictLRU();
            }
            
            long expiryTime = System.currentTimeMillis() + expiryMillis;
            CacheEntry<V> entry = new CacheEntry<>(value, expiryTime);
            cache.put(key, entry);
            accessOrder.put(key, System.currentTimeMillis());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public V get(K key) {
        lock.readLock().lock();
        try {
            CacheEntry<V> entry = cache.get(key);
            if (entry == null || entry.isExpired()) {
                return null;
            }
            accessOrder.put(key, System.currentTimeMillis());
            return entry.value;
        } finally {
            lock.readLock().unlock();
        }
    }

    private void evictLRU() {
        Iterator<Map.Entry<K, Long>> iterator = accessOrder.entrySet().iterator();
        if (iterator.hasNext()) {
            K lruKey = iterator.next().getKey();
            iterator.remove();
            cache.remove(lruKey);
        }
    }

    private void cleanup() {
        lock.writeLock().lock();
        try {
            Iterator<Map.Entry<K, CacheEntry<V>>> iterator = cache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<K, CacheEntry<V>> entry = iterator.next();
                if (entry.getValue().isExpired()) {
                    iterator.remove();
                    accessOrder.remove(entry.getKey());
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void shutdown() {
        cleaner.shutdown();
    }
}