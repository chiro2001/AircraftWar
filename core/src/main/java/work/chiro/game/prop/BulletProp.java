package work.chiro.game.prop;

import work.chiro.game.aircraft.HeroAircraftFactory;
import work.chiro.game.animate.AnimateContainer;
import work.chiro.game.config.AbstractConfig;
import work.chiro.game.thread.MyThreadFactory;
import work.chiro.game.utils.Utils;
import work.chiro.game.vector.Vec2;

/**
 * @author Chiro
 */
public class BulletProp extends AbstractProp {
    public BulletProp( Vec2 posInit, AnimateContainer animateContainer) {
        super(posInit, animateContainer);
    }

    @Override
    public AbstractProp update() {
        playSupplyMusic();
        Utils.getLogger().info("FireSupply active!");
        HeroAircraftFactory.getInstance().increaseShootNum();
        MyThreadFactory.getInstance().newThread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            } finally {
                HeroAircraftFactory.getInstance().decreaseShootNum();
            }
        }).start();
        return this;
    }
}
