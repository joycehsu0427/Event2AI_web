package event.to.ai.backend.connector.adapter.out.persistence.entity;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.connector.domain.ConnectorAnchorSide;
import event.to.ai.backend.connector.domain.ConnectorArrowType;
import event.to.ai.backend.connector.domain.ConnectorLineType;
import event.to.ai.backend.connector.domain.ConnectorTargetType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "connectors")
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "board_id", nullable = false, columnDefinition = "BINARY(16)")
    private Board board;

    @Column(name = "frame_id", columnDefinition = "BINARY(16)")
    private UUID frameId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_target_type", nullable = false, length = 30)
    private ConnectorTargetType fromTargetType;

    @Column(name = "from_target_id", columnDefinition = "BINARY(16)")
    private UUID fromTargetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_side", length = 10)
    private ConnectorAnchorSide fromSide;

    @Column(name = "from_offset")
    private Double fromOffset;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "from_x")),
            @AttributeOverride(name = "y", column = @Column(name = "from_y"))
    })
    private Point2D fromPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_target_type", nullable = false, length = 30)
    private ConnectorTargetType toTargetType;

    @Column(name = "to_target_id", columnDefinition = "BINARY(16)")
    private UUID toTargetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_side", length = 10)
    private ConnectorAnchorSide toSide;

    @Column(name = "to_offset")
    private Double toOffset;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "to_x")),
            @AttributeOverride(name = "y", column = @Column(name = "to_y"))
    })
    private Point2D toPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false, length = 20)
    private ConnectorLineType lineType;

    @Column(length = 500)
    private String label;

    @Column(name = "stroke_color", nullable = false, length = 50)
    private String strokeColor;

    @Column(name = "stroke_width", nullable = false)
    private Double strokeWidth;

    @Column(nullable = false)
    private boolean dashed;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_arrow", nullable = false, length = 20)
    private ConnectorArrowType startArrow;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_arrow", nullable = false, length = 20)
    private ConnectorArrowType endArrow;

    @Column(name = "z_index", nullable = false)
    private Integer zIndex;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Connector() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public UUID getFrameId() {
        return frameId;
    }

    public void setFrameId(UUID frameId) {
        this.frameId = frameId;
    }

    public ConnectorTargetType getFromTargetType() {
        return fromTargetType;
    }

    public void setFromTargetType(ConnectorTargetType fromTargetType) {
        this.fromTargetType = fromTargetType;
    }

    public UUID getFromTargetId() {
        return fromTargetId;
    }

    public void setFromTargetId(UUID fromTargetId) {
        this.fromTargetId = fromTargetId;
    }

    public ConnectorAnchorSide getFromSide() {
        return fromSide;
    }

    public void setFromSide(ConnectorAnchorSide fromSide) {
        this.fromSide = fromSide;
    }

    public Double getFromOffset() {
        return fromOffset;
    }

    public void setFromOffset(Double fromOffset) {
        this.fromOffset = fromOffset;
    }

    public Point2D getFromPoint() {
        return fromPoint;
    }

    public void setFromPoint(Point2D fromPoint) {
        this.fromPoint = fromPoint;
    }

    public ConnectorTargetType getToTargetType() {
        return toTargetType;
    }

    public void setToTargetType(ConnectorTargetType toTargetType) {
        this.toTargetType = toTargetType;
    }

    public UUID getToTargetId() {
        return toTargetId;
    }

    public void setToTargetId(UUID toTargetId) {
        this.toTargetId = toTargetId;
    }

    public ConnectorAnchorSide getToSide() {
        return toSide;
    }

    public void setToSide(ConnectorAnchorSide toSide) {
        this.toSide = toSide;
    }

    public Double getToOffset() {
        return toOffset;
    }

    public void setToOffset(Double toOffset) {
        this.toOffset = toOffset;
    }

    public Point2D getToPoint() {
        return toPoint;
    }

    public void setToPoint(Point2D toPoint) {
        this.toPoint = toPoint;
    }

    public ConnectorLineType getLineType() {
        return lineType;
    }

    public void setLineType(ConnectorLineType lineType) {
        this.lineType = lineType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public Double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public boolean isDashed() {
        return dashed;
    }

    public void setDashed(boolean dashed) {
        this.dashed = dashed;
    }

    public ConnectorArrowType getStartArrow() {
        return startArrow;
    }

    public void setStartArrow(ConnectorArrowType startArrow) {
        this.startArrow = startArrow;
    }

    public ConnectorArrowType getEndArrow() {
        return endArrow;
    }

    public void setEndArrow(ConnectorArrowType endArrow) {
        this.endArrow = endArrow;
    }

    public Integer getZIndex() {
        return zIndex;
    }

    public void setZIndex(Integer zIndex) {
        this.zIndex = zIndex;
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
