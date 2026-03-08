package event.to.ai.backend.board.adapter.out.persistence.entity;

import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "board_memberships",
        indexes = {
                @Index(name = "idx_board_memberships_user_id", columnList = "user_id"),
                @Index(name = "idx_board_memberships_board_id_role", columnList = "board_id,role")
        }
)
public class BoardMembership {

    @EmbeddedId
    private BoardMembershipId id;

    @ManyToOne(optional = false)
    @MapsId("boardId")
    @JoinColumn(name = "board_id", nullable = false, columnDefinition = "BINARY(16)", updatable = false)
    private Board board;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)", updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardMembershipRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public BoardMembership() {
    }

    public BoardMembership(Board board, User user, BoardMembershipRole role) {
        this.board = board;
        this.user = user;
        this.role = role;
        if (board != null && user != null) {
            this.id = new BoardMembershipId(board.getId(), user.getId());
        }
    }

    @PrePersist
    protected void onCreate() {
        if (role == null) {
            role = BoardMembershipRole.VIEWER;
        }
        if (id == null && board != null && user != null) {
            id = new BoardMembershipId(board.getId(), user.getId());
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BoardMembershipId getId() {
        return id;
    }

    public void setId(BoardMembershipId id) {
        this.id = id;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
        if (this.id == null) {
            this.id = new BoardMembershipId();
        }
        this.id.setBoardId(board == null ? null : board.getId());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (this.id == null) {
            this.id = new BoardMembershipId();
        }
        this.id.setUserId(user == null ? null : user.getId());
    }

    public BoardMembershipRole getRole() {
        return role;
    }

    public void setRole(BoardMembershipRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
