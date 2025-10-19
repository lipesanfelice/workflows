import java.lang.annotation.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name() default "";
    boolean primaryKey() default false;
    boolean autoIncrement() default false;
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {
    String name();
}

public class SimpleORM {
    private final Connection connection;

    public SimpleORM(Connection connection) {
        this.connection = connection;
    }

    public <T> void createTable(Class<T> clazz) throws SQLException {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class must be annotated with @Table");
        }

        Table table = clazz.getAnnotation(Table.class);
        List<String> columns = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name().isEmpty() ? field.getName() : column.name();
                String columnType = getSqlType(field.getType());
                
                StringBuilder columnDef = new StringBuilder(columnName + " " + columnType);
                
                if (column.primaryKey()) {
                    columnDef.append(" PRIMARY KEY");
                }
                if (column.autoIncrement()) {
                    columnDef.append(" AUTOINCREMENT");
                }
                
                columns.add(columnDef.toString());
            }
        }

        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s)", 
            table.name(), String.join(", ", columns));

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public <T> void save(T entity) throws Exception {
        Class<?> clazz = entity.getClass();
        Table table = clazz.getAnnotation(Table.class);
        
        List<String> columnNames = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.autoIncrement() && isPrimaryKeyAutoIncrement(entity)) {
                    continue;
                }
                
                field.setAccessible(true);
                String columnName = column.name().isEmpty() ? field.getName() : column.name();
                columnNames.add(columnName);
                placeholders.add("?");
                values.add(field.get(entity));
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
            table.name(), 
            String.join(", ", columnNames),
            String.join(", ", placeholders));

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            
            stmt.executeUpdate();
            
            // Handle auto-increment keys
            if (isPrimaryKeyAutoIncrement(entity)) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        setPrimaryKey(entity, generatedKeys.getObject(1));
                    }
                }
            }
        }
    }

    public <T> List<T> findAll(Class<T> clazz) throws Exception {
        Table table = clazz.getAnnotation(Table.class);
        String sql = "SELECT * FROM " + table.name();
        
        List<T> results = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                T entity = clazz.getDeclaredConstructor().newInstance();
                mapResultSetToEntity(rs, entity);
                results.add(entity);
            }
        }
        
        return results;
    }

    private <T> void mapResultSetToEntity(ResultSet rs, T entity) throws Exception {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name().isEmpty() ? field.getName() : column.name();
                
                field.setAccessible(true);
                field.set(entity, rs.getObject(columnName));
            }
        }
    }

    private String getSqlType(Class<?> type) {
        if (type == String.class) return "TEXT";
        if (type == int.class || type == Integer.class) return "INTEGER";
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == double.class || type == Double.class) return "REAL";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type == Date.class) return "DATETIME";
        return "TEXT";
    }

    private <T> boolean isPrimaryKeyAutoIncrement(T entity) throws Exception {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.primaryKey() && column.autoIncrement()) {
                    return true;
                }
            }
        }
        return false;
    }

    private <T> void setPrimaryKey(T entity, Object key) throws Exception {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.primaryKey()) {
                    field.setAccessible(true);
                    field.set(entity, convertType(key, field.getType()));
                    break;
                }
            }
        }
    }

    private Object convertType(Object value, Class<?> targetType) {
        if (value == null) return null;
        
        if (targetType == Integer.class || targetType == int.class) {
            return ((Number) value).intValue();
        } else if (targetType == Long.class || targetType == long.class) {
            return ((Number) value).longValue();
        }
        
        return value;
    }
}