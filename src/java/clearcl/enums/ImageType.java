package clearcl.enums;

/**
 * OpenCL image type
 *
 * @author royer
 */
public enum ImageType
{
  IMAGE1D, IMAGE2D, IMAGE3D;

  public static ImageType fromDimensions(long[] pDimensions)
  {
    int lDimension = pDimensions.length;
    if (lDimension == 1)
      return IMAGE1D;
    else if (lDimension == 2)
      return IMAGE2D;
    else if (lDimension == 3)
      return IMAGE3D;
    return null;
  }
}
