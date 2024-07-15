package org.example.userauthenticationservice_may.repositories;

import org.example.userauthenticationservice_may.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByValue(String value);
}
