package com.example.demo;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(EmployeeRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(Employee.builder().name("Alice Johnson").email("alice@example.com").department("Engineering").position("Senior Developer").build());
                repo.save(Employee.builder().name("Bob Smith").email("bob@example.com").department("Marketing").position("Marketing Lead").build());
                repo.save(Employee.builder().name("Carol White").email("carol@example.com").department("Engineering").position("QA Engineer").build());
                repo.save(Employee.builder().name("David Brown").email("david@example.com").department("HR").position("HR Manager").build());
            }
        };
    }
}
