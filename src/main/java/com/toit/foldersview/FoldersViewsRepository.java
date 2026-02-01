package com.toit.foldersview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FoldersViewsRepository extends JpaRepository<FoldersViews, Long> {

}