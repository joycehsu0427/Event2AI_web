package event.to.ai.backend.textbox.adapter.out.persistence.entity;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "text_boxes")
public class TextBoxes {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    // 將 Point2D 拆開成 pos_x (覆蓋原本的 x )、pos_y (覆蓋原本的 y )
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "x", column = @Column(name = "pos_x", nullable = false)),
        @AttributeOverride(name = "y", column = @Column(name = "pos_y", nullable = false))
    })
    private Point2D pos;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "x", column = @Column(name = "geo_x", nullable = false)),
        @AttributeOverride(name = "y", column = @Column(name = "geo_y", nullable = false))
    })
    private Point2D geo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "board_id", nullable = false, columnDefinition = "BINARY(16)")
    private Board board;

    @Column(nullable = false)
    private String description;

//    @Column(nullable = false, length = 50)
//    private String color;

    @Column(nullable=false)
    private String tag;

    @Column(name = "font_color")
    private String fontColor;

    @Column(name = "font_size")
    private String fontSize;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public TextBoxes() {
    }
 
    public TextBoxes(Point2D pos) {
        this.pos = pos;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Point2D getPos() {
        return pos;
    }

    public void setPos(Point2D pos) {
        this.pos = pos;
    }

    public Point2D getGeo(){
        return geo;
    }

    public void setGeo(Point2D geo) {
        this.geo = geo;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

//    public String getColor(){
//        return color;
//    }
//
//    public void setColor(String color){
//        this.color = color;
//    }

    public String getTag(){
        return tag;
    }

    public void setTag(String tag){
        this.tag = tag;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
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

    @Override
    public String toString() {
        return "textBoxes{" +
                "id=" + id +
                ", boardId=" + (board == null ? null : board.getId()) +
                ", pos.x=" + pos.getX() +
                ", pos.y=" + pos.getY() +
                ", geo.x=" + geo.getX() +
                ", geo.y=" + geo.getY() +
                ", fontColor=" + fontColor +
                ", fontSize=" + fontSize +
//                ", color=" + color +
                '}';
    }
}
