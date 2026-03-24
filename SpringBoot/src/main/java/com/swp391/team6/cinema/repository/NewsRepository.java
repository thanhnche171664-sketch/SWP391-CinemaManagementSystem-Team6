package com.swp391.team6.cinema.repository;

import com.swp391.team6.cinema.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    boolean existsByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCaseAndNewsIdNot(String title, Long newsId);

    @EntityGraph(attributePaths = {"author", "branch"})
    @Query("""
            select n from News n
            where n.status = :publishedStatus
              and (:keyword is null or trim(:keyword) = '' or lower(n.title) like lower(concat('%', :keyword, '%'))
                   or lower(n.content) like lower(concat('%', :keyword, '%')))
              and (:newsType is null or n.newsType = :newsType)
              and (:branchId is null or n.branchId is null or n.branchId = :branchId)
            """)
    Page<News> searchPublished(@Param("publishedStatus") News.NewsStatus publishedStatus,
                               @Param("keyword") String keyword,
                               @Param("newsType") News.NewsType newsType,
                               @Param("branchId") Long branchId,
                               Pageable pageable);

    @EntityGraph(attributePaths = {"author", "branch"})
    @Query("""
            select n from News n
            where (:keyword is null or trim(:keyword) = '' or lower(n.title) like lower(concat('%', :keyword, '%'))
                   or lower(n.content) like lower(concat('%', :keyword, '%')))
              and (:newsType is null or n.newsType = :newsType)
              and (:newsStatus is null or n.status = :newsStatus)
              and (:branchId is null or n.branchId = :branchId)
            """)
    Page<News> searchForManagement(@Param("keyword") String keyword,
                                   @Param("newsType") News.NewsType newsType,
                                   @Param("newsStatus") News.NewsStatus newsStatus,
                                   @Param("branchId") Long branchId,
                                   Pageable pageable);

    @EntityGraph(attributePaths = {"author", "branch"})
    Optional<News> findWithAuthorAndBranchByNewsId(Long newsId);

    @EntityGraph(attributePaths = {"author", "branch"})
    Optional<News> findWithAuthorAndBranchByNewsIdAndStatus(Long newsId, News.NewsStatus status);
}
