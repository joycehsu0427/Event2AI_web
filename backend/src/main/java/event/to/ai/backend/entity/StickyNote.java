package event.to.ai.backend.entity;

import java.awt.geom.Point2D;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sticky_notes")
public class StickyNote {

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

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable=false)
    private String tag;

    // Constructors
    public StickyNote() {
    }
 
    public StickyNote(Point2D pos) {
        this.pos = pos;
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

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getColor(){
        return color;
    }

    public void setColor(String color){
        this.color = color;
    }

    public String getTag(){
        return tag;
    }

    public void setTag(String tag){
        this.tag = tag;
    }


    @Override
    public String toString() {
        return "StickyNote{" +
                "id=" + id +
                ", pos.x=" + pos.getX() +
                ", pos.y=" + pos.getY() +
                ", geo.x=" + geo.getX() +
                ", geo.y=" + geo.getY() +
                ", color=" + color +
                '}';
    }
}
