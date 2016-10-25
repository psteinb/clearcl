package clearcl.enums;

import coremem.interfaces.SizedInBytes;

/**
 * OpenCL data type
 *
 * @author royer
 */
public enum DataType implements SizedInBytes
{
  Byte(1),
  SignedByte(1),
  Short(2),
  SignedShort(2),
  Integer(4),
  SignedInteger(4),
  Long(8),
  SignedLong(8),
  HalfFloat(2),
  Float(4),
  Double(8);

  private final int mSizeInBytes;

  DataType(int pSizeInBytes)
  {
    mSizeInBytes = pSizeInBytes;
  }

  @Override
  public long getSizeInBytes()
  {
    return mSizeInBytes;
  }
}
