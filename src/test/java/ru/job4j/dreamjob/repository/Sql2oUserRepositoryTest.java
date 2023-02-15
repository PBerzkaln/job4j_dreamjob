package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;

import ru.job4j.dreamjob.model.User;

import java.util.List;
import java.util.Properties;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Sql2oUserRepositoryTest {
    private static Sql2oUserRepository sql2oUserRepository;
    private static Sql2o sql2o;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oUserRepositoryTest.class.getClassLoader()
                .getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");
        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        sql2o = configuration.databaseClient(datasource);
        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void clearRegister() {
        try (Connection connection = sql2o.open()) {
            Query query = connection.createQuery("DELETE FROM users");
            query.executeUpdate();
        }
    }

    @Test
    public void whenSaveThenGetSame() {
        var user = new User(1, "vasya@mail.ru", "Vasya", "123");
        sql2oUserRepository.save(user);
        var userFromBD = sql2oUserRepository.findByEmailAndPassword(
                user.getEmail(), user.getPassword()).get();
        assertThat(userFromBD).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    public void whenSaveSeveralThenGetAll() {
        var user1 = new User(1, "vasya@mail.ru", "Vasya", "123");
        var user2 = new User(2, "petya@mail.ru", "Petya", "456");
        var user3 = new User(3, "kolya@mail.ru", "Kolya", "789");
        sql2oUserRepository.save(user1);
        sql2oUserRepository.save(user2);
        sql2oUserRepository.save(user3);
        var user1FromBD = sql2oUserRepository.findByEmailAndPassword(
                user1.getEmail(), user1.getPassword()).get();
        var user2FromBD = sql2oUserRepository.findByEmailAndPassword(
                user2.getEmail(), user2.getPassword()).get();
        var user3FromBD = sql2oUserRepository.findByEmailAndPassword(
                user3.getEmail(), user3.getPassword()).get();
        assertThat(List.of(user1FromBD, user2FromBD, user3FromBD)).usingRecursiveComparison()
                .isEqualTo(List.of(user1, user2, user3));
    }

    @Test
    public void whenFindNothingAndReturnEmpty() {
        assertThat(sql2oUserRepository.findByEmailAndPassword("kolya@mail.ru", "123").isEmpty());
    }

    @Test
    public void whenFailToSaveTheSameEmail() {
        var user1 = new User(1, "vasya@mail.ru", "Vasya", "123");
        var user2 = new User(2, "vasya@mail.ru", "Petya", "456");
        sql2oUserRepository.save(user1);
        var user2Saved = sql2oUserRepository.save(user2);
        assertThat(user2Saved).isEmpty();
    }
}