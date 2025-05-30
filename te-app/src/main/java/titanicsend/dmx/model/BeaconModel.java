package titanicsend.dmx.model;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.utils.LXUtils;
import titanicsend.dmx.DmxBuffer;
import titanicsend.dmx.parameter.DmxCompoundParameter;
import titanicsend.dmx.parameter.DmxDiscreteParameter;
import titanicsend.dmx.parameter.DmxDiscreteParameterOption;
import titanicsend.dmx.parameter.DmxParameterLimiter.LimitType;

/** Elation Proteus Excalibur moving head beam light */
public class BeaconModel extends DmxModel {

  public static final String MODEL_TYPE = "Beacon";

  /* 0-based index of DMX parameters within DmxBuffer
   * DO NOT skip numbers for multi-byte.
   * This is NOT the same as DMX channel.
   *
   * 16-CHANNEL MODE for ChauvetDJ Beam Q60
   */
  public static final int INDEX_PAN = 0;
  public static final int INDEX_TILT = 1;
  public static final int INDEX_CYAN = 2;
  public static final int INDEX_MAGENTA = 3;
  public static final int INDEX_YELLOW = 4;
  public static final int INDEX_COLOR_WHEEL = 5;
  public static final int INDEX_GOBO1 = 6;
  public static final int INDEX_GOBO1_ROTATION = 7;
  public static final int INDEX_GOBO2 = 8;
  public static final int INDEX_PRISM1 = 9;
  public static final int INDEX_PRISM1_ROTATION = 10;
  public static final int INDEX_PRISM2_ROTATION = 11;
  public static final int INDEX_FOCUS = 12;
  public static final int INDEX_SHUTTER = 13;
  public static final int INDEX_DIMMER = 14;
  public static final int INDEX_FROST1 = 15;
  public static final int INDEX_FROST2 = 16;
  public static final int INDEX_PT_SPEED = 17;
  public static final int INDEX_CONTROL = 18;

  public static final int TILT_MIN = -100;
  public static final int TILT_MAX = 95;

  public static final int SHUTTER_OPEN = 32;
  public static final int STROBE_MIN = 64;
  public static final int STROBE_MAX = 95;

  public static final double DIMMER_NORMALIZED_0 = 0;
  public static final double DIMMER_NORMALIZED_50 = 0.5;
  public static final double DIMMER_NORMALIZED_100 = 1;

  public static final int CONTROL_NORMAL = 0;
  public static final int DEFAULT_FOCUS = 0;

  public static class BeaconConfig extends DmxCommonConfig {
    public float tiltLimit;

    public BeaconConfig setTiltLimit(float tiltLimit) {
      this.tiltLimit = tiltLimit;
      this.channel = 0;
      this.universe = 1;
      return this;
    }
  }

  static BeaconConfig createConfig(LXModel model) {
    LXPoint point = model.points[0];
    BeaconConfig c = new BeaconConfig();
    c.host = model.meta("dmx_host");
    c.tiltLimit = Float.parseFloat(model.meta("tiltLimit"));
    c.x = point.x;
    c.y = point.y;
    c.z = point.z;

    return c;
  }

  /** Dynamic model constructor */
  public BeaconModel(LXModel model) {
    super(model, createConfig(model));
    initialize(((BeaconConfig) this.config).tiltLimit);
  }

  /** Static model constructor */
  public BeaconModel(DmxCommonConfig config, float tiltLimit, String... tags) {
    super(MODEL_TYPE, config, tags);
    initialize(tiltLimit);
  }

  private void initialize(float tiltLimit) {
    DmxCompoundParameter pan = new DmxCompoundParameter("Pan").setNumBytes(2);
    DmxCompoundParameter tilt = new DmxCompoundParameter("Tilt", .5, 0, 1).setNumBytes(2);
    // Apply tilt limit from config file to beacon
    tilt.getLimiter()
        .setLimits(.5 - LXUtils.max(0, tiltLimit / 2), Math.min(1, .5 + (tiltLimit / 2)))
        .setLimitType(LimitType.ZOOM);
    DmxCompoundParameter cyan = new DmxCompoundParameter("Cyan", 0, 0, 100);
    DmxCompoundParameter magenta = new DmxCompoundParameter("Magenta", 0, 0, 100);
    DmxCompoundParameter yellow = new DmxCompoundParameter("Yellow", 0, 0, 100);
    DmxDiscreteParameter colorWheel =
        new DmxDiscreteParameter(
            "ClrWheel",
            new DmxDiscreteParameterOption[] {
              new DmxDiscreteParameterOption("Open", 0),
              new DmxDiscreteParameterOption("Red", 16),
              new DmxDiscreteParameterOption("Green", 23),
              new DmxDiscreteParameterOption("Yellow", 30),
              new DmxDiscreteParameterOption("Purple", 37),
              new DmxDiscreteParameterOption("Light Green", 44),
              new DmxDiscreteParameterOption("Orange", 51),
              new DmxDiscreteParameterOption("Magenta", 58),
              new DmxDiscreteParameterOption("Cyan", 65),
              new DmxDiscreteParameterOption("Open", 72),
              new DmxDiscreteParameterOption("Deep Red", 79),
              new DmxDiscreteParameterOption("Dark Amber", 86),
              new DmxDiscreteParameterOption("Pink", 93),
              new DmxDiscreteParameterOption("UV", 100),
              new DmxDiscreteParameterOption("CTB", 107),
              new DmxDiscreteParameterOption("CTO", 114),
              new DmxDiscreteParameterOption("Medium Blue", 121),
              new DmxDiscreteParameterOption("Scroll CW fast-slow", 128, 180),
              new DmxDiscreteParameterOption("Scroll idle", 181),
              new DmxDiscreteParameterOption("Scroll CCW slow-fast", 182, 234),
              new DmxDiscreteParameterOption("Random Fast", 235),
              new DmxDiscreteParameterOption("Random Medium", 240),
              new DmxDiscreteParameterOption("Random Slow", 245),
              new DmxDiscreteParameterOption("Open", 250)
            });
    // "Rotating Gobo"
    DmxDiscreteParameter gobo1 =
        new DmxDiscreteParameter(
            "Gobo1",
            new DmxDiscreteParameterOption[] {
              new DmxDiscreteParameterOption("Open", 0),
              new DmxDiscreteParameterOption("Spot Open", 11),
              new DmxDiscreteParameterOption("Gobo 1", 22),
              new DmxDiscreteParameterOption("Gobo 12", 32),
              new DmxDiscreteParameterOption("Gobo 3", 42),
              new DmxDiscreteParameterOption("Gobo 4", 52),
              new DmxDiscreteParameterOption("Gobo 5", 62),
              new DmxDiscreteParameterOption("Gobo 6", 72),
              new DmxDiscreteParameterOption("Gobo 7", 82),
              new DmxDiscreteParameterOption("Gobo 8", 92),
              new DmxDiscreteParameterOption("Gobo 1", 102),
              new DmxDiscreteParameterOption("Gobo 2", 113),
              new DmxDiscreteParameterOption("Gobo 3", 124),
              new DmxDiscreteParameterOption("Gobo 4", 135),
              new DmxDiscreteParameterOption("Gobo 5", 146),
              new DmxDiscreteParameterOption("Gobo 6", 157),
              new DmxDiscreteParameterOption("Gobo 7", 168),
              new DmxDiscreteParameterOption("Gobo 8", 179),
              new DmxDiscreteParameterOption("Scroll CW fast-slow", 190, 221),
              new DmxDiscreteParameterOption("Idle", 222),
              new DmxDiscreteParameterOption("Scroll CCW slow-fast", 224, 255)
            });
    // Could use parameter options if they worked with Compound type
    DmxCompoundParameter gobo1rotation =
        new DmxCompoundParameter("Gobo1 Rotation", 0, 0, 255).setNumBytes(2);
    // "Fixed Gobo"
    DmxDiscreteParameter gobo2 =
        new DmxDiscreteParameter(
            "Gobo2",
            new DmxDiscreteParameterOption[] {
              new DmxDiscreteParameterOption("Open", 0),
              new DmxDiscreteParameterOption("Gobo 1", 5),
              new DmxDiscreteParameterOption("Gobo 2", 10),
              new DmxDiscreteParameterOption("Gobo 3", 15),
              new DmxDiscreteParameterOption("Gobo 4", 20),
              new DmxDiscreteParameterOption("Gobo 5", 25),
              new DmxDiscreteParameterOption("Gobo 6", 30),
              new DmxDiscreteParameterOption("Gobo 7", 35),
              new DmxDiscreteParameterOption("Gobo 8", 40),
              new DmxDiscreteParameterOption("Gobo 9", 45),
              new DmxDiscreteParameterOption("Gobo 10", 50),
              new DmxDiscreteParameterOption("Gobo 11", 55),
              new DmxDiscreteParameterOption("Gobo 12", 60),
              new DmxDiscreteParameterOption("Gobo 13", 65),
              new DmxDiscreteParameterOption("Gobo 14", 70),
              new DmxDiscreteParameterOption("Gobo 15", 75),
              new DmxDiscreteParameterOption("Gobo 16", 80),
              new DmxDiscreteParameterOption("Gobo 17", 85),
              new DmxDiscreteParameterOption("Gobo 1 shake slow-fast", 88),
              new DmxDiscreteParameterOption("Gobo 2 shake slow-fast", 94),
              new DmxDiscreteParameterOption("Gobo 3 shake slow-fast", 100),
              new DmxDiscreteParameterOption("Gobo 4 shake slow-fast", 106),
              new DmxDiscreteParameterOption("Gobo 5 shake slow-fast", 112),
              new DmxDiscreteParameterOption("Gobo 6 shake slow-fast", 118),
              new DmxDiscreteParameterOption("Gobo 7 shake slow-fast", 124),
              new DmxDiscreteParameterOption("Gobo 8 shake slow-fast", 130),
              new DmxDiscreteParameterOption("Gobo 9 shake slow-fast", 136),
              new DmxDiscreteParameterOption("Gobo 10 shake slow-fast", 142),
              new DmxDiscreteParameterOption("Gobo 11 shake slow-fast", 148),
              new DmxDiscreteParameterOption("Gobo 12 shake slow-fast", 154),
              new DmxDiscreteParameterOption("Gobo 13 shake slow-fast", 160),
              new DmxDiscreteParameterOption("Gobo 14 shake slow-fast", 166),
              new DmxDiscreteParameterOption("Gobo 15 shake slow-fast", 172),
              new DmxDiscreteParameterOption("Gobo 16 shake slow-fast", 178),
              new DmxDiscreteParameterOption("Gobo 17 shake slow-fast", 184),
              new DmxDiscreteParameterOption("Scroll CW fast-slow", 190, 221),
              new DmxDiscreteParameterOption("Idle", 222),
              new DmxDiscreteParameterOption("Scroll CCW slow-fast", 224, 255)
            });
    DmxDiscreteParameter prism1 =
        new DmxDiscreteParameter(
            "Prism1",
            new DmxDiscreteParameterOption[] {
              new DmxDiscreteParameterOption("Open", 0),
              new DmxDiscreteParameterOption("Beam Expander", 51),
              new DmxDiscreteParameterOption("8 Facet", 101),
              new DmxDiscreteParameterOption("4 Facet Linear", 151),
              new DmxDiscreteParameterOption("8 + 4", 201)
            });
    DmxCompoundParameter prism1rotation =
        new DmxCompoundParameter(
                "Prism1 Rotation",
                new DmxDiscreteParameterOption[] {
                  new DmxDiscreteParameterOption("Indexing", 0, 127),
                  new DmxDiscreteParameterOption("CW rotate fast-slow", 128, 189),
                  new DmxDiscreteParameterOption("No rotate", 190, 193),
                  new DmxDiscreteParameterOption("CCW rotate slow-fast", 194, 255),
                })
            .setNumBytes(2);
    DmxCompoundParameter prism2rotation =
        new DmxCompoundParameter(
                "Prism2 Rotation",
                new DmxDiscreteParameterOption[] {
                  new DmxDiscreteParameterOption("Indexing", 0, 127),
                  new DmxDiscreteParameterOption("CW rotate fast-slow", 128, 189),
                  new DmxDiscreteParameterOption("No rotate", 190, 193),
                  new DmxDiscreteParameterOption("CCW rotate slow-fast", 194, 255),
                })
            .setNumBytes(2);
    DmxCompoundParameter focus = new DmxCompoundParameter("Focus", 0, 0, 255).setNumBytes(2);
    DmxDiscreteParameter shutter =
        new DmxDiscreteParameter(
            "Shutter",
            new DmxDiscreteParameterOption[] {
              new DmxDiscreteParameterOption("Closed", 0),
              new DmxDiscreteParameterOption("Open", 32),
              new DmxDiscreteParameterOption("Strobe slow-fast", STROBE_MIN, STROBE_MAX),
              new DmxDiscreteParameterOption("Open", 96, 127),
              new DmxDiscreteParameterOption("Pulse in sequences", 128, 159), // is range?
              new DmxDiscreteParameterOption("Open", 160),
              new DmxDiscreteParameterOption("Random slow-fast", 192),
              new DmxDiscreteParameterOption("Open", 224)
            });
    DmxCompoundParameter dimmer =
        new DmxCompoundParameter("Dimmer", 0, 0, 100)
            .setNumBytes(2)
            .setScaleToAlpha(true); // Faders applied to this parameter
    DmxCompoundParameter frost1 = new DmxCompoundParameter("Frost1", 0, 0, 100);
    DmxCompoundParameter frost2 = new DmxCompoundParameter("Frost2", 0, 0, 100);
    DmxDiscreteParameter ptSpeed =
        new DmxDiscreteParameter(
            "ptSpd",
            new DmxDiscreteParameterOption[] {
              new DmxDiscreteParameterOption("Fast-Slow", 0, 225),
              new DmxDiscreteParameterOption("Blackout by movement", 226),
              new DmxDiscreteParameterOption("Blackout by all wheel change", 236)
            });
    DmxDiscreteParameter control =
        new DmxDiscreteParameter(
            "Control",
            new DmxDiscreteParameterOption[] {
              new DmxDiscreteParameterOption("Normal", 0),
              new DmxDiscreteParameterOption("Idle", 20),
              new DmxDiscreteParameterOption("Lamp on", 40),
              new DmxDiscreteParameterOption("Lamp off", 50),
              new DmxDiscreteParameterOption("Lamp power 370W", 60),
              new DmxDiscreteParameterOption("Lamp power 430W", 67),
              new DmxDiscreteParameterOption("Lamp power 560W", 74),
              new DmxDiscreteParameterOption("All motor reset", 80),
              new DmxDiscreteParameterOption("Pan Tilt motor reset", 85),
              new DmxDiscreteParameterOption("Color motor reset", 88),
              new DmxDiscreteParameterOption("Gobo motor reset", 91),
              new DmxDiscreteParameterOption("Shutter & Dimmer motor reset", 94),
              new DmxDiscreteParameterOption("Other motor reset", 97),
              new DmxDiscreteParameterOption("Idle", 100),
              new DmxDiscreteParameterOption("CMY Normal", 165),
              new DmxDiscreteParameterOption("CMY Fast (default)", 167),
              new DmxDiscreteParameterOption("Vent Cleaning ON", 169),
              new DmxDiscreteParameterOption("Vent Cleaning OFF", 171),
              new DmxDiscreteParameterOption("Hibernation OFF", 173),
              new DmxDiscreteParameterOption("Hibernation", 175),
              new DmxDiscreteParameterOption("Sun Protect ON", 177),
              new DmxDiscreteParameterOption("Sun Protect OFF", 178),
              new DmxDiscreteParameterOption("Idle", 179),
              new DmxDiscreteParameterOption("Pan Tilt Smooth (default)", 181),
              new DmxDiscreteParameterOption("Pan Tilt Fast", 191),
              new DmxDiscreteParameterOption("Dimmer Curve Linear (default)", 201),
              new DmxDiscreteParameterOption("Dimmer Curve Square", 211),
              new DmxDiscreteParameterOption("Dimmer Curve Inverse Square", 221),
              new DmxDiscreteParameterOption("Dimmer Curve S-Curve", 231),
              new DmxDiscreteParameterOption("Internal Program 1", 241),
              new DmxDiscreteParameterOption("Internal Program 2", 242),
              new DmxDiscreteParameterOption("Internal Program 3", 243),
              new DmxDiscreteParameterOption("Internal Program 4", 244),
              new DmxDiscreteParameterOption("Internal Program 5", 245),
              new DmxDiscreteParameterOption("Internal Program 6", 246),
              new DmxDiscreteParameterOption("Internal Program 7", 247),
              new DmxDiscreteParameterOption("Idle", 248),
              new DmxDiscreteParameterOption("Display OFF", 250),
              new DmxDiscreteParameterOption("Display ON", 252),
              new DmxDiscreteParameterOption("Idle", 254)
            });

    addField(new FieldDefinition(pan));
    addField(new FieldDefinition(tilt));
    addField(new FieldDefinition(cyan));
    addField(new FieldDefinition(magenta));
    addField(new FieldDefinition(yellow));
    addField(new FieldDefinition(colorWheel));
    addField(new FieldDefinition(gobo1));
    addField(new FieldDefinition(gobo1rotation));
    addField(new FieldDefinition(gobo2));
    addField(new FieldDefinition(prism1));
    addField(new FieldDefinition(prism1rotation));
    addField(new FieldDefinition(prism2rotation));
    addField(new FieldDefinition(focus));
    addField(new FieldDefinition(shutter));
    addField(new FieldDefinition(dimmer));
    addField(new FieldDefinition(frost1));
    addField(new FieldDefinition(frost2));
    addField(new FieldDefinition(ptSpeed));
    addField(new FieldDefinition(control));
  }

  @Override
  public void validate(DmxBuffer buffer) {
    // Do any cross-field safety checking here.
  }
}
