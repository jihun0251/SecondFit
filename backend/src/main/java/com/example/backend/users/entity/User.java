package com.example.backend.users.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        // DDL: uk_users_email, uk_users_nickname — 제약조건 이름까지 맞춰둔다
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자 필요, 외부 무분별 생성은 막음
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password; // BCrypt 해시가 저장됨

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING) // ENUM을 문자열로 저장 ('USER'/'ADMIN')
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 20)
    private String phone;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    /** 판매자 정산 계좌 (PATCH /users/me). 최초 DDL에 누락되어 추가한 컬럼 */
    @Column(name = "settlement_account", length = 100)
    private String settlementAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @CreationTimestamp // insert 시 자동 기록
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // update 시 자동 갱신
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private User(String email, String password, String nickname, String phone) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
        this.role = Role.USER;       // 가입 시 기본 USER
        this.status = Status.ACTIVE; // 가입 시 기본 ACTIVE
    }

    /**
     * 프로필 부분 수정 (PATCH). null인 필드는 "변경 안 함".
     * 닉네임 중복 검사는 서비스에서 먼저 하고 들어온다.
     */
    public void updateProfile(String nickname, String phone, String profileImage, String settlementAccount) {
        if (nickname != null) this.nickname = nickname;
        if (phone != null) this.phone = phone;
        if (profileImage != null) this.profileImage = profileImage;
        if (settlementAccount != null) this.settlementAccount = settlementAccount;
    }

    /**
     * 회원 탈퇴.
     * 레코드를 지우지 않고 상태만 바꾼다 — 주문/정산 이력이 FK로 물려 있어서
     * 실제 삭제하면 과거 거래 기록이 통째로 무너지기 때문.
     */
    public void withdraw() {
        this.status = Status.WITHDRAWN;
    }

    public enum Role { USER, ADMIN }
    public enum Status { ACTIVE, SUSPENDED, WITHDRAWN }
}