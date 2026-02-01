package com.toit.folders;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FoldersRepository extends  JpaRepository<Folders, Long>{
    List<Folders> findByUsers_UsersId(Long usersId);
}
