package kr.co.wanted.posts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity {
    @CreatedDate
    @Column(insertable = true, updatable = false, columnDefinition = "datetime(6) default now(6)")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = true, updatable = true, columnDefinition = "datetime(6) default now(6)")
    private LocalDateTime updatedAt;
}
