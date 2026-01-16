package com.toit.repository;

import com.toit.entity.Folders;
import com.toit.entity.Schedules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FoldersRepository extends  JpaRepository<Folders, Long>{

}
