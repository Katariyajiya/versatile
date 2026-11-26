package com.versatile.repository;

import com.versatile.entity.InterviewSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<InterviewSessionEntity,String> {
}
