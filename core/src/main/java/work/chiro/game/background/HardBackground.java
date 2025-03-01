package work.chiro.game.background;

import work.chiro.game.animate.AnimateContainer;
import work.chiro.game.vector.Vec2;

import java.util.Random;

/**
 * @author Chiro
 */
public class HardBackground extends AbstractBackground {
    public HardBackground() {
        super();
    }

    public HardBackground(Vec2 posInit, AnimateContainer animateContainer) {
        super(posInit, animateContainer);
    }

    @Override
    String getInitImageFilename() {
        return "bg" + (new Random().nextInt(2) + 4) + ".jpg";
    }

    @Override
    AbstractBackground newInstance(Vec2 posInit, AnimateContainer animateContainer) {
        return new HardBackground(posInit, animateContainer);
    }
}
