package com.ls.spaceBookingSystem.repository;

import com.ls.spaceBookingSystem.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, Long> {

}