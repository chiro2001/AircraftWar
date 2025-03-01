package work.chiro.game.history;

import java.util.List;
import java.util.Optional;

import work.chiro.game.config.Difficulty;

/**
 * 历史记录数据对象接口
 * @author Chiro
 */
public interface HistoryDAO {
    /**
     * 获取所有历史数据
     * @return 历史数据列表
     */
    List<HistoryObject> getAll();

    /**
     * 使用名字查询列表
     * @param name 名称
     * @return 查询到的数据项目
     */
    Optional<HistoryObject> getByName(String name);

    /**
     * 使用 time 为主键更新，newHistory 的 time 会被更新
     * @param time 指定的 time
     * @param newHistory 新的数据
     * @return 是否更新成功
     */
    Boolean updateByTime(long time, HistoryObject newHistory);

    /**
     * 使用 time 为主键删除对应项目
     * @param time 指定的 time
     * @return 是否删除成功
     */
    Boolean deleteByTime(long time);

    /**
     * 删除所有项目
     */
    void deleteAll();

    /**
     * 添加一个项目
     * @param historyObject 项目
     */
    void addOne(HistoryObject historyObject);

    /**
     * 写入磁盘数据
     */
    void dump();

    /**
     * 读取磁盘数据
     */
    void load();

    /**
     * 排序内部数据
     */
    void sort();

    /**
     * 直接设置内部数据
     * @param data 数据列表
     */
    void set(List<HistoryObject> data);

    /**
     * 依据难度筛选数据
     * @param difficulty 难度
     * @return 数据列表
     */
    List<HistoryObject> getByDifficulty(Difficulty difficulty);
}
