package com.example.gatepass.repository;


import com.example.gatepass.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    List<Visitor> findByNameContainingIgnoreCase(
            String name);

}