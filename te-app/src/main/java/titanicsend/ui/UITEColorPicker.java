/**
 * This file is mainly a copy Heron Art's P4LX UIColorPicker:
 * https://github.com/heronarts/P4LX/blob/master/src/main/java/heronarts/p4lx/ui/component/UIColorPicker.java
 * ...Additional controls were added at the bottom for TE.
 *
 * <p>This file could be simplified in the future with a few changes to the restrictions in
 * UIColorPicker.
 *
 * <p>Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * <p>This file is part of the LX Studio software library. By using LX, you agree to the terms of
 * the LX Studio Software License and Distribution Agreement, available at: http://lx.studio/license
 *
 * <p>Please note that the LX license is not open-source. The license allows for free,
 * non-commercial use.
 *
 * <p>HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE, AND SPECIFICALLY
 * DISCLAIMS ANY WARRANTY OF MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR PURPOSE,
 * WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */
package titanicsend.ui;

import heronarts.glx.event.KeyEvent;
import heronarts.glx.event.MouseEvent;
import heronarts.glx.ui.UI;
import heronarts.glx.ui.UI2dComponent;
import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.UIColor;
import heronarts.glx.ui.UIFocus;
import heronarts.glx.ui.UITimerTask;
import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UIDoubleBox;
import heronarts.glx.ui.component.UIKnob;
import heronarts.glx.ui.component.UILabel;
import heronarts.glx.ui.component.UIParameterControl;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.color.LXColor;
import heronarts.lx.command.LXCommand;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.pattern.UIGradientPattern.UIPaletteGradient;
import heronarts.lx.utils.LXUtils;
import titanicsend.color.TEColorParameter;

public class UITEColorPicker extends UI2dComponent {

  public enum Corner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_RIGHT,
    BOTTOM_LEFT
  };

  private Corner corner = Corner.BOTTOM_RIGHT;

  private static float INDICATOR_HEIGHT = 4;
  private static float INDICATOR_WIDTH = 6;

  private TEColorParameter color;

  private UITEColorOverlay uiColorOverlay = null;

  private boolean enabled = true;

  private int drawColor = LXColor.BLACK;

  private boolean deviceMode = false;

  public UITEColorPicker(TEColorParameter color) {
    this(UIKnob.WIDTH, UIKnob.WIDTH, color);
  }

  public UITEColorPicker(float w, float h, TEColorParameter color) {
    this(0, 0, w, h, color);
  }

  public UITEColorPicker(float x, float y, float w, float h, TEColorParameter color) {
    this(x, y, w, h, color, false);
  }

  protected UITEColorPicker(
      float x, float y, float w, float h, TEColorParameter color, boolean isDynamic) {
    super(x, y, w, h);
    setColor(color);

    // Redraw with color in real-time, if modulated
    if (!isDynamic) {
      setDescription(UIParameterControl.getDescription(color));
      addLoopTask(
          new UITimerTask(30, UITimerTask.Mode.FPS) {
            @Override
            protected void run() {
              setDrawColor(color.calcColor());
            }
          });
    }
  }

  protected void setDrawColor(int drawColor) {
    if (this.drawColor != drawColor) {
      this.drawColor = drawColor;
      redraw();
    }
  }

  public UITEColorPicker setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  protected UITEColorPicker setDeviceMode(boolean deviceMode) {
    this.deviceMode = deviceMode;
    return this;
  }

  private final LXParameterListener redrawSwatch =
      (p) -> {
        if (this.uiColorOverlay != null) {
          this.uiColorOverlay.swatch.redraw();
          this.uiColorOverlay.swatchDimmer.redraw();
        }
      };

  protected UITEColorOverlay buildColorOverlay(UI ui) {
    return new UITEColorOverlay(ui);
  }

  public UITEColorPicker setCorner(Corner corner) {
    this.corner = corner;
    return this;
  }

  void setColor(TEColorParameter color) {
    if (this.color != null) {
      this.color.removeListener(this.redrawSwatch);
    }
    this.color = color;
    if (color != null) {
      color.addListener(this.redrawSwatch);
      redrawSwatch.onParameterChanged(color);
    } else {
      setDrawColor(LXColor.BLACK);
    }
  }

  @Override
  public void drawBorder(UI ui, VGraphics vg) {
    if (this.deviceMode) {
      vg.beginPath();
      vg.strokeColor(ui.theme.controlBorderColor);
      vg.rect(
          UIKnob.KNOB_MARGIN + .5f,
          INDICATOR_HEIGHT + .5f,
          UIKnob.KNOB_SIZE - 1f,
          UIKnob.KNOB_SIZE - (INDICATOR_HEIGHT * 2) - 1f);
      vg.stroke();
    } else {
      super.drawBorder(ui, vg);
    }
  }

  @Override
  public void onDraw(UI ui, VGraphics vg) {
    vg.strokeWidth(1f);

    // Gradient
    for (float x = UIKnob.KNOB_MARGIN + 1f; x < this.width - UIKnob.KNOB_MARGIN; ++x) {
      float lerp = (x - UIKnob.KNOB_MARGIN) / UIKnob.KNOB_SIZE;
      vg.beginPath();
      vg.strokeColor(this.color.getGradientColorFixed(lerp));
      vg.moveTo(x, INDICATOR_HEIGHT + 1f);
      vg.lineTo(x, UIKnob.KNOB_SIZE - INDICATOR_HEIGHT - 1);
      vg.stroke();
    }

    // Indicators
    float offset = this.color.getOffsetf();
    float indicatorX = UIKnob.KNOB_MARGIN + .5f + (UIKnob.KNOB_SIZE * offset);
    drawTriangle(
        ui, this, vg, indicatorX, INDICATOR_HEIGHT, INDICATOR_WIDTH, INDICATOR_HEIGHT, true);
    drawTriangle(
        ui,
        this,
        vg,
        indicatorX,
        UIKnob.KNOB_SIZE - INDICATOR_HEIGHT,
        INDICATOR_WIDTH,
        INDICATOR_HEIGHT,
        false);

    if (this.deviceMode) {
      UIParameterControl.drawParameterLabel(
          ui, vg, this, this.color != null ? this.color.getLabel() : "-");
    }
  }

  private void drawTriangle(
      UI ui,
      UI2dComponent component,
      VGraphics vg,
      float x,
      float y,
      float w,
      float h,
      boolean invert) {
    vg.fillColor(this.enabled ? ui.theme.controlTextColor : ui.theme.controlDisabledTextColor);
    vg.beginPath();
    vg.moveTo(x - (w / 2), y + (invert ? 0 - h : h));
    vg.lineTo(x + (w / 2), y + (invert ? 0 - h : h));
    vg.lineTo(x, y);
    vg.closePath();
    vg.fill();
  }

  protected void hideOverlay() {
    getUI().clearContextOverlay(this.uiColorOverlay);
  }

  private void showOverlay() {
    final float overlap = 6;

    if (this.uiColorOverlay == null) {
      this.uiColorOverlay = buildColorOverlay(getUI());
    }

    switch (this.corner) {
      case BOTTOM_LEFT:
        this.uiColorOverlay.setPosition(
            this, overlap - this.uiColorOverlay.getWidth(), this.height - overlap);
        break;
      case BOTTOM_RIGHT:
        this.uiColorOverlay.setPosition(this, this.width - overlap, this.height - overlap);
        break;
      case TOP_LEFT:
        this.uiColorOverlay.setPosition(
            this,
            overlap - this.uiColorOverlay.getWidth(),
            overlap - this.uiColorOverlay.getHeight());
        break;
      case TOP_RIGHT:
        this.uiColorOverlay.setPosition(
            this, this.width - overlap, overlap - this.uiColorOverlay.getHeight());
        break;
    }

    getUI().showContextOverlay(this.uiColorOverlay);
  }

  @Override
  public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    if (this.enabled) {
      mouseEvent.consume();
      showOverlay();
    }
    super.onMousePressed(mouseEvent, mx, my);
  }

  @Override
  public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if (this.enabled) {
      if (keyEvent.isEnter() || (keyCode == KeyEvent.VK_SPACE)) {
        keyEvent.consume();
        showOverlay();
      } else if (keyCode == KeyEvent.VK_ESCAPE) {
        if ((this.uiColorOverlay != null) && (this.uiColorOverlay.isVisible())) {
          keyEvent.consume();
          hideOverlay();
        }
      }
    }
    super.onKeyPressed(keyEvent, keyChar, keyCode);
  }

  @Override
  public void dispose() {
    if (this.color != null) {
      this.color.removeListener(this.redrawSwatch);
    }
    if (this.uiColorOverlay != null) {
      getUI().clearContextOverlay(this.uiColorOverlay);
      this.uiColorOverlay.dispose();
    }
    super.dispose();
  }

  protected class UITEColorOverlay extends UI2dContainer {

    private static final int PADDING = 8;

    private final UISwatch swatch;
    private final UI2dComponent swatchDimmer;
    private final UI2dComponent color1;
    private final UI2dComponent color2;

    UITEColorOverlay(UI ui) {
      this(ui, 324, color instanceof TEColorParameter ? 60 : 8);
    }

    UITEColorOverlay(UI ui, float width, float extraHeight) {
      super(0, 0, width, UISwatch.HEIGHT + extraHeight);

      setBackgroundColor(ui.theme.deviceBackgroundColor);
      setBorderColor(ui.theme.controlBorderColor);
      setBorderRounding(6);

      this.swatch = new UISwatch();
      this.swatch.addToContainer(this);

      float xp = this.swatch.getX() + this.swatch.getWidth();
      float yp = 16;
      final UIDoubleBox hueBox =
          (UIDoubleBox) new UIDoubleBox(xp, yp, 56, color.hue).addToContainer(this);
      new UILabel(xp, yp + 16, 56, "Hue")
          .setTextAlignment(VGraphics.Align.CENTER)
          .addToContainer(this);

      yp += 40;

      final UIDoubleBox satBox =
          (UIDoubleBox) new UIDoubleBox(xp, yp, 56, color.saturation).addToContainer(this);
      new UILabel(xp, yp + 16, 56, "Sat")
          .setTextAlignment(VGraphics.Align.CENTER)
          .addToContainer(this);

      yp += 40;

      final UIDoubleBox brtBox =
          (UIDoubleBox) new UIDoubleBox(xp, yp, 56, color.brightness).addToContainer(this);
      new UILabel(xp, yp + 16, 56, "Bright")
          .setTextAlignment(VGraphics.Align.CENTER)
          .addToContainer(this);

      // Horizontal break
      new UI2dComponent(PADDING, 140, this.width - 2 * PADDING, 1) {}.setBorderColor(
              ui.theme.controlBorderColor)
          .addToContainer(this);

      // Special for TEColorParameter

      // Gray overlay for swatch when not using STATIC source
      swatchDimmer =
          new UILabel(PADDING, PADDING, this.width - 2 * PADDING, 135)
              .setBackgroundColor(LXColor.rgba(75, 75, 75, 200))
              .addToContainer(this);

      // Offset
      new UIKnob(4, 147, color.offset).addToContainer(this);

      // Solid color
      UI2dContainer.newHorizontalContainer(
              16,
              4,
              new UILabel(40, 12, "Source:").setFont(ui.theme.getControlFont()),
              new UIButton(65, 16, color.colorSource))
          .setPosition(58, 148)
          .addToContainer(this);

      // Blend
      UI2dContainer.newHorizontalContainer(
              16,
              4,
              new UILabel(40, 12, "Blend:").setFont(ui.theme.getControlFont()),
              new UIButton(65, 16, color.blendMode))
          .setPosition(58, 171)
          .addToContainer(this);

      // Solid color preview
      UI2dContainer.newHorizontalContainer(
              16,
              1,
              color1 =
                  new UI2dComponent(0, 0, 16, 16) {}.setBorderColor(ui.theme.controlBorderColor)
                      .setBackgroundColor(color.calcColor())
                      .setBorderRounding(4),
              color2 =
                  new UI2dComponent(0, 0, 16, 16) {}.setBorderColor(ui.theme.controlBorderColor)
                      .setBackgroundColor(color.calcColor2())
                      .setBorderRounding(4))
          .setPosition(292, 148)
          .setVisible(false)
          .addToContainer(this);

      addListener(
          color,
          p -> {
            color1.setBackgroundColor(color.calcColor());
            color2.setBackgroundColor(color.calcColor2());
          });

      // Gradient preview
      new UIPaletteGradient((LXStudio.UI) ui, color, 64, 28)
          .setPosition(178, 154)
          .addToContainer(this);

      color.colorSource.addListener(
          (p) -> {
            boolean isStatic = color.colorSource.getEnum() == TEColorParameter.ColorSource.STATIC;
            swatch.setEnabled(isStatic);
            hueBox.setEnabled(isStatic);
            satBox.setEnabled(isStatic);
            brtBox.setEnabled(isStatic);
            swatchDimmer.setVisible(!isStatic);
          },
          true);
    }

    private class UISwatch extends UI2dComponent implements UIFocus {

      private static final float PADDING = 12;

      private static final float GRID_X = PADDING;
      private static final float GRID_Y = PADDING;

      private static final float GRID_WIDTH = 120;
      private static final float GRID_HEIGHT = 120;

      private static final float BRIGHT_SLIDER_X = 140;
      private static final float BRIGHT_SLIDER_Y = PADDING;
      private static final float BRIGHT_SLIDER_WIDTH = 16;
      private static final float BRIGHT_SLIDER_HEIGHT = GRID_HEIGHT;

      private static final float WIDTH = BRIGHT_SLIDER_X + BRIGHT_SLIDER_WIDTH + 2 * PADDING;
      private static final float HEIGHT = GRID_HEIGHT + 2 * PADDING;

      private boolean enabled = true;

      public UISwatch() {
        super(4, 4, WIDTH, HEIGHT);
        color.addListener(
            (p) -> {
              redraw();
            });
        setFocusCorners(false);
      }

      private void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }

      @Override
      public void onDraw(UI ui, VGraphics vg) {
        final int xStops = 6;
        final int yStops = 40;
        final float xStep = GRID_WIDTH / xStops;
        final float yStep = GRID_HEIGHT / yStops;

        float hue = color.hue.getBaseValuef();
        float saturation = color.saturation.getBaseValuef();
        float brightness = color.brightness.getBaseValuef();

        // Main color grid
        for (int y = 0; y < yStops; ++y) {
          for (int x = 0; x < xStops; ++x) {
            vg.fillLinearGradient(
                GRID_X + x * xStep,
                0,
                GRID_X + (x + 1) * xStep,
                0,
                LXColor.hsb(x * 360 / xStops, 100f - y * 100f / yStops, brightness),
                LXColor.hsb((x + 1) * 360 / xStops, 100f - y * 100f / yStops, brightness));
            vg.beginPath();
            vg.rect(GRID_X + x * xStep - .5f, GRID_Y + y * yStep - .5f, xStep + 1, yStep + 1);
            vg.fill();
          }
        }

        // Brightness slider
        vg.fillLinearGradient(
            BRIGHT_SLIDER_X,
            BRIGHT_SLIDER_Y,
            BRIGHT_SLIDER_X,
            BRIGHT_SLIDER_HEIGHT,
            LXColor.hsb(hue, saturation, 100),
            LXColor.hsb(hue, saturation, 0));
        vg.beginPath();
        vg.rect(
            BRIGHT_SLIDER_X, BRIGHT_SLIDER_Y - .5f, BRIGHT_SLIDER_WIDTH, BRIGHT_SLIDER_HEIGHT + 1);
        vg.fill();

        // Color square
        vg.beginPath();
        vg.strokeColor(brightness < 50 ? UIColor.WHITE : UIColor.BLACK);
        vg.ellipse(
            GRID_X + hue / 360 * GRID_WIDTH, GRID_Y + (1 - saturation / 100) * GRID_HEIGHT, 4, 4);
        vg.stroke();

        // Brightness triangle
        vg.beginPath();
        vg.fillColor(ui.theme.controlTextColor);
        float xp = BRIGHT_SLIDER_X;
        float yp = BRIGHT_SLIDER_Y + (1 - brightness / 100) * BRIGHT_SLIDER_HEIGHT;
        vg.moveTo(xp, yp);
        vg.lineTo(xp - 6, yp - 4);
        vg.lineTo(xp - 6, yp + 4);
        vg.closePath();
        vg.moveTo(xp + BRIGHT_SLIDER_WIDTH, yp);
        vg.lineTo(xp + BRIGHT_SLIDER_WIDTH + 6, yp + 4);
        vg.lineTo(xp + BRIGHT_SLIDER_WIDTH + 6, yp - 4);
        vg.closePath();
        vg.fill();
      }

      private boolean draggingBrightness = false;
      private LXCommand.Parameter.SetValue setBrightness = null;
      private LXCommand.Parameter.SetColor setColor = null;

      @Override
      public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        if (!this.enabled) {
          return;
        }

        this.setBrightness = null;
        this.setColor = null;
        if (this.draggingBrightness = (mx > GRID_X + GRID_WIDTH)) {
          this.setBrightness =
              new LXCommand.Parameter.SetValue(color.brightness, color.brightness.getBaseValue());
        } else {
          this.setColor = new LXCommand.Parameter.SetColor(color);
          setHueSaturation(mx, my);
        }
      }

      @Override
      public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
        this.setBrightness = null;
        this.setColor = null;
      }

      private void setHueSaturation(float mx, float my) {
        mx = LXUtils.clampf(mx - GRID_X, 0, GRID_WIDTH);
        my = LXUtils.clampf(my - GRID_Y, 0, GRID_WIDTH);
        double hue = mx / GRID_WIDTH * 360;
        double saturation = 100 - my / GRID_HEIGHT * 100;
        getLX().command.perform(this.setColor.update(hue, saturation));
      }

      @Override
      public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        if (!this.enabled) {
          return;
        }
        if (this.draggingBrightness) {
          if (dy != 0) {
            float brightness = color.brightness.getBaseValuef();
            brightness = LXUtils.clampf(brightness - 100 * dy / BRIGHT_SLIDER_HEIGHT, 0, 100);
            getLX().command.perform(this.setBrightness.update(brightness));
          }
        } else {
          setHueSaturation(mx, my);
        }
      }

      @Override
      public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
        if (!this.enabled) {
          return;
        }
        float inc = keyEvent.isShiftDown() ? 10 : 2;
        if (keyCode == java.awt.event.KeyEvent.VK_UP) {
          keyEvent.consume();
          getLX()
              .command
              .perform(
                  new LXCommand.Parameter.SetValue(
                      color.saturation,
                      LXUtils.clampf(color.saturation.getBaseValuef() + inc, 0, 100)));
        } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
          keyEvent.consume();
          getLX()
              .command
              .perform(
                  new LXCommand.Parameter.SetValue(
                      color.saturation,
                      LXUtils.clampf(color.saturation.getBaseValuef() - inc, 0, 100)));
        } else if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
          keyEvent.consume();
          getLX()
              .command
              .perform(
                  new LXCommand.Parameter.SetValue(
                      color.hue, LXUtils.clampf(color.hue.getBaseValuef() - 3 * inc, 0, 360)));
        } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
          keyEvent.consume();
          getLX()
              .command
              .perform(
                  new LXCommand.Parameter.SetValue(
                      color.hue, LXUtils.clampf(color.hue.getBaseValuef() + 3 * inc, 0, 360)));
        }
      }
    }
  }
}
