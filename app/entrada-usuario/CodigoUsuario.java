import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {
    String topic() default "";
    boolean async() default false;
    int priority() default 0;
}

public class AdvancedEventBus {
    private final Map<String, List<Subscriber>> subscribers;
    private final ExecutorService asyncExecutor;
    private final boolean enableLogging;

    public AdvancedEventBus(boolean enableLogging) {
        this.subscribers = new ConcurrentHashMap<>();
        this.asyncExecutor = Executors.newCachedThreadPool();
        this.enableLogging = enableLogging;
    }

    private class Subscriber implements Comparable<Subscriber> {
        final Object target;
        final Method method;
        final boolean async;
        final int priority;

        Subscriber(Object target, Method method, boolean async, int priority) {
            this.target = target;
            this.method = method;
            this.async = async;
            this.priority = priority;
            method.setAccessible(true);
        }

        void handleEvent(Object event) {
            if (async) {
                asyncExecutor.submit(() -> invokeMethod(event));
            } else {
                invokeMethod(event);
            }
        }

        private void invokeMethod(Object event) {
            try {
                method.invoke(target, event);
            } catch (Exception e) {
                if (enableLogging) {
                    System.err.println("Error invoking subscriber: " + e.getMessage());
                }
            }
        }

        @Override
        public int compareTo(Subscriber other) {
            return Integer.compare(other.priority, this.priority);
        }
    }

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Subscribe annotation = method.getAnnotation(Subscribe.class);
                String topic = annotation.topic();
                boolean async = annotation.async();
                int priority = annotation.priority();

                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1) {
                    throw new IllegalArgumentException("Subscriber method must have exactly one parameter");
                }

                Subscriber subscriber = new Subscriber(listener, method, async, priority);
                subscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(subscriber);
                
                // Sort by priority
                subscribers.get(topic).sort(Subscriber::compareTo);
            }
        }
    }

    public void post(Object event) {
        post("", event);
    }

    public void post(String topic, Object event) {
        List<Subscriber> topicSubscribers = subscribers.getOrDefault(topic, new ArrayList<>());
        
        if (enableLogging) {
            System.out.println("Dispatching event to " + topicSubscribers.size() + " subscribers");
        }

        for (Subscriber subscriber : topicSubscribers) {
            subscriber.handleEvent(event);
        }
    }

    public void shutdown() {
        asyncExecutor.shutdown();
    }
}