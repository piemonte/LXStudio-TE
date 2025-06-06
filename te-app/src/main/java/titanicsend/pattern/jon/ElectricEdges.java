package titanicsend.pattern.jon;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import titanicsend.pattern.glengine.GLShaderPattern;
import titanicsend.pattern.yoffa.framework.TEShaderView;

@LXCategory("Native Shaders Edges")
public class ElectricEdges extends GLShaderPattern {

  public ElectricEdges(LX lx) {
    super(lx, TEShaderView.ALL_EDGES);

    // Set control range -- this uses the same shader as the electric panel
    // pattern, but it is parameterized *very* differently.
    controls.setRange(TEControlTag.SIZE, 0.05, 0.005, 0.4); // noise scale
    controls.setRange(TEControlTag.SPEED, 1, -2, 2); // arc wave speed
    controls.setRange(TEControlTag.QUANTITY, 0.5, 0.5, 1); // base glow level
    controls.setRange(TEControlTag.WOW1, 0.8, 0, 2.6); // radial coord distortion
    controls.setRange(TEControlTag.WOW2, 0.02, 0.0, 0.2); // noise field amplitude
    controls.setRange(TEControlTag.SPIN, 0.6, -3, 3); // arc spin rate

    // register common controls with LX
    addCommonControls();

    addShader("electric.fs");
  }
}
