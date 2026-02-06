package com.toit.folders;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FoldersRepository extends  JpaRepository<Folders, Long>{
    List<Folders> findByUsers_UsersId(Long usersId);

    Optional<Folders> findByFoldersIdAndUsers_UsersId(Long foldersId, Long usersId);
}
