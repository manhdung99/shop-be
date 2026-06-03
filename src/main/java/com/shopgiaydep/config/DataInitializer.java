package com.shopgiaydep.config;

import com.shopgiaydep.entity.Admin;
import com.shopgiaydep.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Tạo admin mặc định nếu chưa có
        if (!adminRepository.existsByUsername("admin")) {
            Admin admin = Admin.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@shopgiaydep.vn")
                    .fullName("Admin")
                    .build();
            adminRepository.save(admin);
            log.info("✅ Đã tạo tài khoản admin mặc định: admin / admin123");
        }
    }
}
