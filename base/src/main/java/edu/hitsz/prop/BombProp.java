package edu.hitsz.prop;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.animate.AnimateContainer;
import edu.hitsz.vector.Vec2;

import java.util.List;

/**
 * @author Chiro
 */
public class BombProp extends AbstractProp {
    public BombProp(Vec2 posInit, AnimateContainer animateContainer) {
        super(posInit, animateContainer);
    }

    @Override
    public void handleAircrafts(List<AbstractAircraft> enemyAircrafts) {
        System.out.println("BombSupply active!");
        for (AbstractAircraft enemy : enemyAircrafts) {
            enemy.vanish();
        }
    }
}
