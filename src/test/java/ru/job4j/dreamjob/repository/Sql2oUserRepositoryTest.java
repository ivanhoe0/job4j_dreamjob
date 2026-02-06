package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import java.util.Properties;

import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class Sql2oUserRepositoryTest {

    private static Sql2oUserRepository repository;

    @BeforeAll
    public static void initConnection() throws Exception {
        var properties = new Properties();
        try (var input = Sql2oUserRepository.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(input);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var dataSource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(dataSource);

        repository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void clear() {
        repository.clearTable();
    }

    @Test
    void whenSaveThenGetOk() {
        var user = repository.save(new User(0, "example@com", "John", "password"));
        var savedUser = repository.findByEmailAndPassword(user.get().getEmail(), user.get().getPassword());
        assertThat(user.get()).usingRecursiveAssertion().isEqualTo(savedUser.get());
    }

    @Test
    void whenSaveWithSameEmailThenEmptyUser() {
        var user = repository.save(new User(0, "example@com", "John", "password"));
        var savedUser = repository.save(user.get());
        assertThat(savedUser).isEqualTo(empty());
    }
}