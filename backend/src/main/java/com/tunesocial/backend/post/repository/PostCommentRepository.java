package com.tunesocial.backend.post.repository;

import com.tunesocial.backend.post.model.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    Page<PostComment> findAllByPostIdAndParentIdIsNull(Long postId, Pageable pageable);

    List<PostComment> findAllByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByParentId(Long parentId);

    @Query(value = """
        SELECT c.* FROM post_comments c 
        LEFT JOIN reactions r ON r.target_id = c.id AND r.target_type = 'COMMENT'
        WHERE c.post_id = :postId AND c.parent_id IS NULL
        GROUP BY c.id
        ORDER BY COUNT(r.id) DESC, c.created_at DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<PostComment> findTopCommentByPostId(Long postId);

    @Modifying
    @Transactional
    void deleteAllByParentId(Long parentId);

    @Query("SELECT c.id FROM PostComment c WHERE c.parentId = :parentId")
    List<Long> findAllIdsByParentId(@Param("parentId") Long parentId);
}
