package clearcl.interfaces;

import java.util.concurrent.CopyOnWriteArrayList;

import coremem.interfaces.SizedInBytes;
import coremem.types.NativeTypeEnum;

public interface ClearCLImageInterface extends SizedInBytes
{

  public NativeTypeEnum getNativeType();

  public default long getPixelSizeInBytes()
  {
    return getNativeType().getSizeInBytes()*getNumberOfChannels();
  }

  public long getNumberOfChannels();

  /**
   * Returns this image dimensions.
   * 
   * @return dimensions
   */
  public long[] getDimensions();

  /**
   * Returns this image width
   * 
   * @return width
   */
  public default long getWidth()
  {
    return getDimensions()[0];
  }

  /**
   * Returns this image height
   * 
   * @return height
   */
  public default long getHeight()
  {
    if (getDimensions().length < 2)
      return 1;
    return getDimensions()[1];
  }

  /**
   * Returns this image depth
   * 
   * @return depth
   */
  public default long getDepth()
  {
    if (getDimensions().length < 3)
      return 1;
    return getDimensions()[2];
  }

  /**
   * Return this image dimension (1D, 2D, or 3D).
   * 
   * @return image dimension (1, 2, or 3)
   */
  public default long getDimension()
  {
    return getDimensions().length;
  }

  /**
   * Returns this image volume
   * 
   * @return depth
   */
  public default long getVolume()
  {
    return getWidth() * getHeight() * getDepth();
  }

  
}
