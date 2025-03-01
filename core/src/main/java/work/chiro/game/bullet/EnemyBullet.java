package work.chiro.game.bullet;

import work.chiro.game.animate.AnimateContainer;
import work.chiro.game.config.AbstractConfig;
import work.chiro.game.vector.Vec2;

/**
 * @author hitsz
 */
public class EnemyBullet extends BaseBullet {
    public EnemyBullet(AbstractConfig config, Vec2 posInit, AnimateContainer animateContainer, int power) {
        super(config, posInit, animateContainer, power);
    }
}
