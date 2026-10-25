package com.skillsphere.skill.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.skillsphere.skill.entity.InternalJob;

public interface InternalJobRepository extends JpaRepository<InternalJob, Long> {
    List<InternalJob> findByStatusIgnoreCase(String status);
}
