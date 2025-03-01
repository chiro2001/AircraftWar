package work.chiro.game.history;

import work.chiro.game.config.Difficulty;

/**
 * @author Chiro
 */
public class HistoryObjectFactory {
    final private String name;
    final private double score;
    final private long time;
    final private String message;
    final private Difficulty difficulty;

    public HistoryObjectFactory(String name, double score, String message, Difficulty difficulty) {
        this.name = name;
        this.score = score;
        this.message = message;
        this.difficulty = difficulty;
        this.time = System.currentTimeMillis();
    }

    public HistoryObject create() {
        return new HistoryObject(name, score, time, message, difficulty);
    }
}
