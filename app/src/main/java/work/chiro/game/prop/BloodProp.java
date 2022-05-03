package work.chiro.game.prop;

import work.chiro.game.aircraft.HeroAircraftFactory;
import work.chiro.game.animate.AnimateContainer;
import work.chiro.game.vector.Vec2;

/**
 * @author Chiro
 */
public class BloodProp extends AbstractProp implements PropApplier {
    int increaseHp;
    public BloodProp(Vec2 posInit, AnimateContainer animateContainer, int increaseHp) {
        super(posInit, animateContainer);
        this.increaseHp = increaseHp;
    }

    @Override
    public AbstractProp update() {
        playSupplyMusic();
        HeroAircraftFactory.getInstance().decreaseHp(-increaseHp);
        return this;
    }
}
