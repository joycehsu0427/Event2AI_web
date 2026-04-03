package event.to.ai.backend.connector.adapter.in.web.dto;

import event.to.ai.backend.connector.domain.ConnectorAnchorSide;
import event.to.ai.backend.connector.domain.ConnectorArrowType;
import event.to.ai.backend.connector.domain.ConnectorLineType;
import event.to.ai.backend.connector.domain.ConnectorTargetType;

import java.util.UUID;

public class ConnectorDTO {

    private UUID id;
    private UUID boardId;
    private UUID frameId;
    private ConnectorTargetType fromTargetType;
    private UUID fromTargetId;
    private ConnectorAnchorSide fromSide;
    private Double fromOffset;
    private Double fromX;
    private Double fromY;
    private ConnectorTargetType toTargetType;
    private UUID toTargetId;
    private ConnectorAnchorSide toSide;
    private Double toOffset;
    private Double toX;
    private Double toY;
    private ConnectorLineType lineType;
    private String label;
    private String strokeColor;
    private Double strokeWidth;
    private boolean dashed;
    private ConnectorArrowType startArrow;
    private ConnectorArrowType endArrow;
    private Integer zIndex;

    public ConnectorDTO() {
    }

    public ConnectorDTO(UUID id, UUID boardId, UUID frameId,
                        ConnectorTargetType fromTargetType, UUID fromTargetId, ConnectorAnchorSide fromSide,
                        Double fromOffset, Double fromX, Double fromY,
                        ConnectorTargetType toTargetType, UUID toTargetId, ConnectorAnchorSide toSide,
                        Double toOffset, Double toX, Double toY,
                        ConnectorLineType lineType, String label, String strokeColor, Double strokeWidth,
                        boolean dashed, ConnectorArrowType startArrow, ConnectorArrowType endArrow, Integer zIndex) {
        this.id = id;
        this.boardId = boardId;
        this.frameId = frameId;
        this.fromTargetType = fromTargetType;
        this.fromTargetId = fromTargetId;
        this.fromSide = fromSide;
        this.fromOffset = fromOffset;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toTargetType = toTargetType;
        this.toTargetId = toTargetId;
        this.toSide = toSide;
        this.toOffset = toOffset;
        this.toX = toX;
        this.toY = toY;
        this.lineType = lineType;
        this.label = label;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.dashed = dashed;
        this.startArrow = startArrow;
        this.endArrow = endArrow;
        this.zIndex = zIndex;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
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

    public Double getFromX() {
        return fromX;
    }

    public void setFromX(Double fromX) {
        this.fromX = fromX;
    }

    public Double getFromY() {
        return fromY;
    }

    public void setFromY(Double fromY) {
        this.fromY = fromY;
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

    public Double getToX() {
        return toX;
    }

    public void setToX(Double toX) {
        this.toX = toX;
    }

    public Double getToY() {
        return toY;
    }

    public void setToY(Double toY) {
        this.toY = toY;
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
}
