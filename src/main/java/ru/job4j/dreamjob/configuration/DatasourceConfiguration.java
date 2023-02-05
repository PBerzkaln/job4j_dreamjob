package ru.job4j.dreamjob.configuration;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;
import org.sql2o.quirks.NoQuirks;
import org.sql2o.quirks.Quirks;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Configuration аннотация по аналогии с Service, Repository
 * имеет лишь семантическую значимость.
 * Она указывает, что данный класс является классом,
 * который производит настройку приложения.
 */
@Configuration
public class DatasourceConfiguration {
    /**
     * Bean аннотация будет подробнее рассмотрена в блоке про Spring.
     * Сейчас важно понимать, что с помощью нее мы указываем,
     * что наш метод создает объект какого-то класса,
     * который будет использоваться при внедрении зависимости.
     * <br>Value берут значения для подключения,
     * которые мы указали в application.properties.
     * <br>Пул создается в методе connectionPool.
     * В качестве его реализации выступает класс BasicDataSource.
     *
     * @param url
     * @param username
     * @param password
     * @return
     */
    @Bean
    public DataSource connectionPool(@Value("${datasource.url}") String url,
                                     @Value("${datasource.username}") String username,
                                     @Value("${datasource.password}") String password) {
        return new BasicDataSource() {
            {
                setUrl(url);
                setUsername(username);
                setPassword(password);
            }
        };
    }

    /**
     * Метод databaseClient создает экземляр Sql2o.
     *
     * @param dataSource
     * @return
     */
    @Bean
    public Sql2o databaseClient(DataSource dataSource) {
        return new Sql2o(dataSource, createConverters());
    }

    /**
     * Создает конвертер, который делает преобразование из Timestamp
     * в LocalDateTime и наоборот. Помните, как мы это делали в блоке JDBC?
     * Тут идея такая, что теперь мы создали конвертер,
     * который будет делать преобразования вместо того,
     * чтобы каждый раз дублировать логику.
     * Этот конвертер будет использоваться Sql2o.
     *
     * @return
     */
    private Quirks createConverters() {
        return new NoQuirks() {
            {
                converters.put(LocalDateTime.class, new Converter<LocalDateTime>() {

                    @Override
                    public LocalDateTime convert(Object value) throws ConverterException {
                        if (value == null) {
                            return null;
                        }
                        if (!(value instanceof Timestamp)) {
                            throw new ConverterException("Invalid value to convert");
                        }
                        return ((Timestamp) value).toLocalDateTime();
                    }

                    @Override
                    public Object toDatabaseParam(LocalDateTime value) {
                        return value == null ? null : Timestamp.valueOf(value);
                    }

                });
            }
        };
    }
}