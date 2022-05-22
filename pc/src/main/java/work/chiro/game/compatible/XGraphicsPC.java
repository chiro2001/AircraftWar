package work.chiro.game.compatible;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import work.chiro.game.config.RunningConfig;

public abstract class XGraphicsPC implements XGraphics {
    double alpha = 1.0;
    double rotation = 0.0;
    int color = 0x0;

    @Override
    public XImage<?> drawImage(XImage<?> image, double x, double y) {
        AffineTransform af = AffineTransform.getTranslateInstance(x, y);
        af.rotate(rotation, image.getWidth() * 1.0 / 2, image.getHeight() * 1.0 / 2);
        Graphics2D graphics2D = (Graphics2D) (getGraphics());
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) alpha));
        graphics2D.drawImage((Image) image.getImage(), af, null);
        return image;
    }

    @Override
    public XImage<?> drawImage(XImage<?> image, double x, double y, double w, double h) {
        if (x + w < 0 || y + h < 0 || x > RunningConfig.windowWidth || y > RunningConfig.windowHeight) {
            return image;
        }
        if (image.getWidth() != (int) w || image.getHeight() != (int) h) {
            BufferedImage raw = (BufferedImage) image.getImage();
            Image resizedImage = raw.getScaledInstance((int) w, (int) h, Image.SCALE_DEFAULT);
            BufferedImage bufferedImage = new BufferedImage((int) w, (int) h, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = bufferedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(resizedImage, 0, 0, (int) w, (int) h, null);
            g.dispose();
            return drawImage(new XImageFactoryPC().create(bufferedImage), x, y);
        } else {
            return drawImage(image, x, y);
        }
    }

    @Override
    public XGraphics setAlpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    @Override
    public XGraphics setRotation(double rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public XGraphics setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public XGraphics fillRect(double x, double y, double width, double height) {
        getGraphics().setColor(new Color(color));
        getGraphics().fillRect((int) x, (int) y, (int) width, (int) height);
        return this;
    }

    @Override
    public XGraphics drawString(String text, double x, double y) {
        getGraphics().drawString(text, (int) x, (int) y);
        return this;
    }

    abstract protected Graphics getGraphics();
}
