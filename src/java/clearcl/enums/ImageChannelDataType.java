package clearcl.enums;

/**
 * OpenCL image channel data type
 *
 * @author royer
 */
public enum ImageChannelDataType
{
  SignedNormalizedInt8(DataType.SignedByte, true),
  SignedNormalizedInt16(DataType.SignedShort, true),
  UnsignedNormalizedInt8(DataType.Byte, true),
  UnsignedNormalizedInt16(DataType.Short, true),
  SignedInt8(DataType.SignedByte, false),
  SignedInt16(DataType.SignedShort, false),
  SignedInt32(DataType.SignedInteger, false),
  UnsignedInt8(DataType.SignedByte, false),
  UnsignedInt16(DataType.SignedShort, false),
  UnsignedInt32(DataType.SignedInteger, false),
  HalfFloat(DataType.HalfFloat, false),
  Float(DataType.Float, false);

  DataType mDataType;
  boolean mIsNormalized;

  private ImageChannelDataType(DataType pDataType,
                               boolean pIsNormalized)
  {
    mDataType = pDataType;
    mIsNormalized = pIsNormalized;
  }

  public DataType getDataType()
  {
    return mDataType;
  }

  public boolean isNormalized()
  {
    return mIsNormalized;
  }
}
