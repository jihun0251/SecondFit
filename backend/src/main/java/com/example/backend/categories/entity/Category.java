package com.example.backend.categories.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "categories",
        // DDL: UNIQUE KEY uk_categories_name (name), KEY idx_categories_parent (parent_id)
        uniqueConstraints = @UniqueConstraint(name = "uk_categories_name", columnNames = "name"),
        indexes = @Index(name = "idx_categories_parent", columnList = "parent_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    // 부모 방향: 나는 하나의 부모를 가진다 (아우터를 가리킴)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // 자식 방향: 나는 여러 자식을 가진다 ([데님 자켓, 코트])
    // @OrderBy가 없으면 자식들의 순서가 DB 반환 순서에 맡겨져 화면 순서가 들쭉날쭉해진다
    @OneToMany(mappedBy = "parent")
    @OrderBy("sortOrder ASC, id ASC")
    private List<Category> children = new ArrayList<>();
}