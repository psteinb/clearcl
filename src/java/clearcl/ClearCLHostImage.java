package clearcl;

import java.util.Arrays;

import clearcl.abs.ClearCLBase;
import clearcl.abs.ClearCLMemBase;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.interfaces.ClearCLImageInterface;
import clearcl.interfaces.ClearCLMemInterface;
import coremem.ContiguousMemoryInterface;
import coremem.offheap.OffHeapMemory;
import coremem.types.NativeTypeEnum;
import coremem.util.Size;

public class ClearCLHostImage extends ClearCLMemBase implements
                                                    ClearCLMemInterface,
                                                    ClearCLImageInterface
{

  private final ContiguousMemoryInterface mContiguousMemory;
  private NativeTypeEnum mNativeType;
  private final long[] mDimensions;
  private long mNumberOfChannels;

  public static ClearCLHostImage allocateSameAs(ClearCLImage pClearCLImage)
  {
    ClearCLHostImage lClearCLHostImage = new ClearCLHostImage(pClearCLImage.getBackend(),
                                                              OffHeapMemory.allocateBytes(pClearCLImage.getSizeInBytes()),
                                                              pClearCLImage.getChannelDataType()
                                                                           .getNativeType(),
                                                              pClearCLImage.getChannelOrder()
                                                                           .getNumberOfChannels(),
                                                              pClearCLImage.getDimensions());
    return lClearCLHostImage;
  }

  public ClearCLHostImage(ClearCLBackendInterface pClearCLBackendInterface,
                          NativeTypeEnum pNativeType,
                          long pNumberOfChannels,
                          long... pDimensions)
  {
    this(pClearCLBackendInterface,
         OffHeapMemory.allocateBytes(pNumberOfChannels * Size.of(pNativeType)
                                     * getVolume(pDimensions)),
         pNativeType,
         pNumberOfChannels,
         pDimensions);

  }

  public ClearCLHostImage(ClearCLBackendInterface pClearCLBackendInterface,
                          ContiguousMemoryInterface pContiguousMemoryInterface,
                          NativeTypeEnum pNativeType,
                          long pNumberOfChannels,
                          long... pDimensions)
  {
    super(pClearCLBackendInterface,
          pClearCLBackendInterface.wrap(pContiguousMemoryInterface));
    mContiguousMemory = pContiguousMemoryInterface;
    mNativeType = pNativeType;
    mNumberOfChannels = pNumberOfChannels;
    mDimensions = pDimensions;
  }

  public ContiguousMemoryInterface getContiguousMemory()
  {
    return mContiguousMemory;
  }

  public long[] getDimensions()
  {
    return mDimensions;
  }

  public NativeTypeEnum getNativeType()
  {
    return mNativeType;
  }

  public long getPixelSizeInBytes()
  {
    return mNativeType.getSizeInBytes();
  }

  public long getNumberOfChannels()
  {
    return mNumberOfChannels;
  }

  public void writeTo(byte[] pArray)
  {
    mContiguousMemory.copyTo(pArray);
  }

  private static final long getVolume(long[] pDimensions)
  {
    long lVolume = 1;
    for (int i = 0; i < pDimensions.length; i++)
      lVolume *= pDimensions[i];
    return lVolume;
  }

  /* (non-Javadoc)
   * @see coremem.interfaces.SizedInBytes#getSizeInBytes()
   */
  @Override
  public long getSizeInBytes()
  {
    return mContiguousMemory.getSizeInBytes();
  }

  @Override
  public String toString()
  {
    return String.format("ClearCLHostImage [mContiguousMemory=%s, getDimensions()=%s, getNativeType()=%s, getPixelSizeInBytes()=%s, getNumberOfChannels()=%s, getSizeInBytes()=%s]",
                         mContiguousMemory,
                         Arrays.toString(getDimensions()),
                         getNativeType(),
                         getPixelSizeInBytes(),
                         getNumberOfChannels(),
                         getSizeInBytes());
  }

  @Override
  public void close()
  {
    mContiguousMemory.free();
  }

}
