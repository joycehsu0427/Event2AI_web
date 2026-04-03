package event.to.ai.backend.analysis.domain;

import java.awt.geom.Point2D;

public final class StickyNote {
    private final String id;
    private Point2D pos; // the position of Card
    private Point2D geo; // the geometry of Card
    private String description;
    private String color;
    private String tag = null;
    private String frameId = null;

    public StickyNote(String id, String description,  Point2D pos, Point2D geo,  String color) {
        this.id = id;
        this.description = description;
        this.pos = pos;
        this.geo = geo;
        this.color = color;
    }

    public StickyNote(String id, String description,  Point2D pos, Point2D geo,  String color, String tag) {
        this.id = id;
        this.description = description;
        this.pos = pos;
        this.geo = geo;
        this.color = color;
        this.tag = tag;
    }

    public StickyNote(String id, String description, Point2D pos, Point2D geo, String color, String tag, String frameId) {
        this.id = id;
        this.description = description;
        this.pos = pos;
        this.geo = geo;
        this.color = color;
        this.tag = tag;
        this.frameId = frameId;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Point2D getPos() {
        return pos;
    }

    public void setPos(Point2D pos) {
        this.pos = pos;
    }

    public Point2D getGeo() {
        return geo;
    }

    public void setGeo(Point2D geo) {
        this.geo = geo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

}
