package edu.hitsz.basic

import edu.hitsz.aircraft.AbstractAircraft
import edu.hitsz.animate.AnimateContainer
import edu.hitsz.application.ImageResourceReady
import edu.hitsz.basic.PositionType.Position
import edu.hitsz.utils.getTimeMills

/**
 * 可飞行对象的父类
 *
 * @author chiro2001
 */
abstract class FlyingObject(posInit: Position, animateContainer: AnimateContainer[Vec2Double])
  extends ImageResourceReady {
  println(s"Object created at $posInit ${getClass.getName}")
  protected var pos = posInit

  def getPos = pos

  def setPos(posNew: Position) = pos.set(posNew)

  def setPos(posX: Double, posY: Double) = pos.set(new Position(posX, posY))

  /**
   * x 轴长度，根据图片尺寸获得
   * -1 表示未设置
   */
  protected var width = -1
  /**
   * y 轴长度，根据图片尺寸获得
   * -1 表示未设置
   */
  protected var height = -1
  /**
   * 有效（生存）标记，
   * 通常标记为 false的对象会再下次刷新时清除
   */
  protected var valid = true

  /**
   * 可飞行对象根据速度移动
   * 若飞行对象触碰到横向边界，横向速度反向
   */
  def forward(): Unit = {
    animateContainer.updateAll(getTimeMills)
  }

  /**
   * 碰撞检测，当对方坐标进入我方范围，判定我方击中<br>
   * 对方与我方覆盖区域有交叉即判定撞击。
   * <br>
   * 非飞机对象区域：
   * 横向，[x - width/2, x + width/2]
   * 纵向，[y - height/2, y + height/2]
   * <br>
   * 飞机对象区域：
   * 横向，[x - width/2, x + width/2]
   * 纵向，[y - height/4, y + height/4]
   *
   * @param flyingObject 撞击对方
   * @return true: 我方被击中; false 我方未被击中
   */
  def crash(flyingObject: FlyingObject) = {
    // 缩放因子，用于控制 y轴方向区域范围
    val factor = if (this.isInstanceOf[AbstractAircraft]) 2 else 1
    val fFactor = if (flyingObject.isInstanceOf[AbstractAircraft]) 2 else 1
    val x = flyingObject.getLocationX
    val y = flyingObject.getLocationY
    val fWidth = flyingObject.getWidth
    val fHeight = flyingObject.getHeight
    x + (fWidth + getWidth) / 2 > getLocationX &&
      x - (fWidth + getWidth) / 2 < getLocationX &&
      y + (fHeight / fFactor + getHeight / factor) / 2 > getLocationY &&
      y - (fHeight / fFactor + getHeight / factor) / 2 < getLocationY
  }

  def getLocationX = getPos.getX

  def getLocationY = getPos.getY

  def setLocation(locationX: Double, locationY: Double) = setPos(locationX, locationY)

  def getSpeedY = animateContainer.getSpeed(getTimeMills).get(1)

  def getWidth = {
    // 若未设置，则查询图片宽度并设置
    if (width == -1) width = getImage.getWidth
    width
  }

  def getHeight = {
    // 若未设置，则查询图片高度并设置
    if (height == -1) height = getImage.getHeight
    height
  }

  def isValid = this.valid

  /**
   * 标记消失，
   * isValid = false.
   * notValid() => true.
   */
  def vanish() = valid = false
}