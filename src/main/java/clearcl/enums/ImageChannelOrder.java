package clearcl.enums;

/**
 * OpenCl image channel order.
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum ImageChannelOrder
{

  //TODO: use SingleChannel, DoubleChannel, TripleChannel, QuadChannel
  //      in the backend for any working implementation (this is device specific)
  //      this is useful for testing purposes - especially for SingleChannel
  SingleChannel(1),
 Intensity(1),
 Luminance(1),
 R(1),
 A(1),
  DoubleChannel(2),
 RG(2),
 RA(2),
  TripleChannel(3),
 RGB(3),
  QuadChannel(4),
 RGBA(4),
 ARGB(4),
 BGRA(4);

  private final int mNumberOfChannels;

  private ImageChannelOrder(int pNumberOfChannels)
  {
    mNumberOfChannels = pNumberOfChannels;
  }

  /**
   * Returns the number of channels
   * 
   * @return number of channels
   */
  public int getNumberOfChannels()
  {
    return mNumberOfChannels;
  }

}
