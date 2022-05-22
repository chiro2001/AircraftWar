package work.chiro.game.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import work.chiro.game.animate.AnimateContainer;
import work.chiro.game.basic.AbstractObject;
import work.chiro.game.compatible.XGraphics;
import work.chiro.game.utils.Utils;
import work.chiro.game.vector.Vec2;

public abstract class XView extends AbstractObject {
    protected String text = "";
    protected String id = "View" + Utils.idGenerator();
    private final Map<XEventType, XViewCallback> listeners = new HashMap<>();
    private List<Vec2> box = null;
    private String imageResource = null;

    public XView(Vec2 posInit, Vec2 sizeInit) {
        super(posInit, new AnimateContainer(), sizeInit);
    }

    public XView(Vec2 posInit) {
        super(posInit, new AnimateContainer());
    }

    public XView setImageResource(String imageResource) {
        this.imageResource = imageResource;
        return this;
    }

    public XView setText(String text) {
        this.text = text;
        return this;
    }

    public XView setId(String id) {
        this.id = id;
        return this;
    }

    public XView setBox(List<Vec2> box) {
        this.box = box;
        return this;
    }

    public XView trigger(XEvent event) {
        listeners.forEach((eventType, callback) -> {
            if (eventType == event.getEventType()) callback.onEvent(this, event);
        });
        return this;
    }

    public XView addListener(XEventType eventType, XViewCallback callback) {
        listeners.put(eventType, callback);
        return this;
    }

    public XView setOnClick(XViewCallback callback) {
        return addListener(XEventType.Touch, callback);
    }

    @Override
    protected String getImageFilename() {
        if (imageResource == null)
            return super.getImageFilename();
        else return imageResource;
    }

    @Override
    public void draw(XGraphics g) {
        super.draw(g, false);
    }

    @Override
    public void vanish() {
        // 不会消失
    }
}
