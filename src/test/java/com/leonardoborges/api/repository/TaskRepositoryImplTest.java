package com.leonardoborges.api.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("TaskRepositoryImpl Tests")
class TaskRepositoryImplTest {
    
    @Test
    @DisplayName("TaskRepositoryImpl requires integration test with real database")
    void shouldRequireIntegrationTest() {
    }
}
