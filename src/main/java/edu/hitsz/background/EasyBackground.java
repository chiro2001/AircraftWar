package edu.hitsz.background;

import edu.hitsz.animate.AnimateContainer;
import edu.hitsz.vector.Vec2;

import java.util.Random;

/**
 * @author Chiro
 */
public class EasyBackground extends BasicBackground {
    public EasyBackground() {
        super();
    }

    public EasyBackground(Vec2 posInit, AnimateContainer animateContainer) {

        super(posInit, animateContainer);
    }

    @Override
    String getInitImageFilename() {
        return "bg.jpg";
    }

    @Override
    AbstractBackground newInstance(Vec2 posInit, AnimateContainer animateContainer) {
        return new EasyBackground(posInit, animateContainer);
    }
}
