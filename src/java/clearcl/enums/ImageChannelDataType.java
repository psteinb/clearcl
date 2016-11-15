package clearcl.enums;

import coremem.enums.NativeTypeEnum;

/**
 * OpenCL image channel data type
 *
 * @author royer
 */
public enum ImageChannelDataType
{
  SignedNormalizedInt8(NativeTypeEnum.Byte, true),
  SignedNormalizedInt16(NativeTypeEnum.Short, true),
  UnsignedNormalizedInt8(NativeTypeEnum.UnsignedByte, true),
  UnsignedNormalizedInt16(NativeTypeEnum.UnsignedShort, true),
  SignedInt8(NativeTypeEnum.Byte, false),
  SignedInt16(NativeTypeEnum.Short, false),
  SignedInt32(NativeTypeEnum.Int, false),
  UnsignedInt8(NativeTypeEnum.UnsignedByte, false),
  UnsignedInt16(NativeTypeEnum.UnsignedShort, false),
  UnsignedInt32(NativeTypeEnum.UnsignedInt, false),
  HalfFloat(NativeTypeEnum.HalfFloat, false),
  Float(NativeTypeEnum.Float, false);

  NativeTypeEnum mNativeType;
  boolean mIsNormalized;

  private ImageChannelDataType(NativeTypeEnum pDataType,
                               boolean pIsNormalized)
  {
    mNativeType = pDataType;
    mIsNormalized = pIsNormalized;
  }

  public NativeTypeEnum getNativeType()
  {
    return mNativeType;
  }

  public boolean isNormalized()
  {
    return mIsNormalized;
  }
}
