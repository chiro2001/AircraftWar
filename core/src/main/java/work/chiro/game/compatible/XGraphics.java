package work.chiro.game.compatible;

public interface XGraphics {
    /**
     * 在 (x, y) 绘制图片
     *
     * @param image 图片
     * @param x     X坐标
     * @param y     Y坐标
     * @return this
     */
    XImage<?> drawImage(XImage<?> image, double x, double y);

    /**
     * 在 (x, y) 绘制大小为 (w, h) 的图片
     * @param image 图片
     * @param x X坐标
     * @param y Y坐标
     * @param w 宽度
     * @param h 高度
     * @return this
     */
    XImage<?> drawImage(XImage<?> image, double x, double y, double w, double h);

    /**
     * 设置绘制图片时的透明度
     *
     * @param alpha 透明度 (0~1)
     * @return this
     */
    XGraphics setAlpha(double alpha);

    /**
     * 设置绘制图片时的旋转角度
     *
     * @param rotation 旋转角度
     * @return this
     */
    XGraphics setRotation(double rotation);

    /**
     * 设置绘制图形颜色
     * @param color 颜色
     * @return this
     */
    XGraphics setColor(int color);

    /**
     * 绘制一个填充矩形
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @return this
     */
    XGraphics fillRect(double x, double y, double width, double height);

    /**
     * 绘制文字
     * @param text 文字
     * @param x X坐标
     * @param y Y坐标
     * @return this
     */
    XGraphics drawString(String text, double x, double y);
}
