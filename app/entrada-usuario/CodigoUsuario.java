import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.prefs.Preferences;

public class ConfigurationManager {
    
    private Map<String, Object> config = new ConcurrentHashMap<>();
    private Set<ConfigurationListener> listeners = new CopyOnWriteArraySet<>();
    private WatchService watchService;
    private File configFile;
    private ScheduledExecutorService reloadExecutor;
    
    public interface ConfigurationListener {
        void onConfigChanged(String key, Object oldValue, Object newValue);
    }
    
    public ConfigurationManager(String configFilePath) throws IOException {
        this.configFile = new File(configFilePath);
        this.watchService = FileSystems.getDefault().newWatchService();
        this.reloadExecutor = Executors.newScheduledThreadPool(1);
        
        loadConfiguration();
        setupFileWatcher();
    }
    
    private void loadConfiguration() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                Object oldValue = config.put(key, parseValue(value));
                notifyListeners(key, oldValue, config.get(key));
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }
    
    private Object parseValue(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Não é inteiro
        }
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // Não é double
        }
        
        return value;
    }
    
    private void setupFileWatcher() throws IOException {
        Path configPath = configFile.toPath().getParent();
        configPath.register(watchService, 
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_CREATE);
        
        reloadExecutor.scheduleAtFixedRate(() -> {
            try {
                WatchKey key = watchService.poll();
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().toString().equals(configFile.getName())) {
                            loadConfiguration();
                            break;
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                System.err.println("Error in file watcher: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    public void addListener(ConfigurationListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(ConfigurationListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners(String key, Object oldValue, Object newValue) {
        for (ConfigurationListener listener : listeners) {
            listener.onConfigChanged(key, oldValue, newValue);
        }
    }
    
    public Object get(String key) {
        return config.get(key);
    }
    
    public String getString(String key, String defaultValue) {
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    public int getInt(String key, int defaultValue) {
        Object value = config.get(key);
        return value instanceof Number ? ((Number) value).intValue() : defaultValue;
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = config.get(key);
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }
    
    public void shutdown() {
        try {
            watchService.close();
        } catch (IOException e) {
            System.err.println("Error closing watch service: " + e.getMessage());
        }
        reloadExecutor.shutdown();
    }
}