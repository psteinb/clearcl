package clearcl;

import java.util.Arrays;

import clearcl.abs.ClearCLMemBase;
import clearcl.enums.HostAccessType;
import clearcl.interfaces.ClearCLImageInterface;
import clearcl.interfaces.ClearCLMemInterface;
import coremem.ContiguousMemoryInterface;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;

public class ClearCLHostImageBuffer extends ClearCLMemBase implements
                                    ClearCLMemInterface,
                                    ClearCLImageInterface
{

  private final ContiguousMemoryInterface mContiguousMemory;
  private NativeTypeEnum mNativeType;
  private final long[] mDimensions;
  private long mNumberOfChannels;

  public static ClearCLHostImageBuffer allocateSameAs(ClearCLImageInterface pClearCLImage)
  {
    ClearCLHostImageBuffer lClearCLHostImage =
                                             new ClearCLHostImageBuffer(pClearCLImage.getContext(),
                                                                        allocate(pClearCLImage.getSizeInBytes()),
                                                                        pClearCLImage.getNativeType(),
                                                                        pClearCLImage.getNumberOfChannels(),
                                                                        pClearCLImage.getDimensions());
    return lClearCLHostImage;
  }

  public ClearCLHostImageBuffer(ClearCLContext pClearCLContext,
                                NativeTypeEnum pNativeType,
                                long pNumberOfChannels,
                                long... pDimensions)
  {
    this(pClearCLContext,
         allocate(pNumberOfChannels * Size.of(pNativeType)
                  * getVolume(pDimensions)),
         pNativeType,
         pNumberOfChannels,
         pDimensions);

  }

  private static OffHeapMemory allocate(long pSizeInBytes)
  {
    return OffHeapMemory.allocatePageAlignedBytes("ClearCLHostImageBuffer",
                                                  pSizeInBytes);
  }

  public ClearCLHostImageBuffer(ClearCLContext pClearCLContext,
                                ContiguousMemoryInterface pContiguousMemoryInterface,
                                NativeTypeEnum pNativeType,
                                long pNumberOfChannels,
                                long... pDimensions)
  {
    super(pClearCLContext.getBackend(),
          pClearCLContext.getBackend()
                         .wrap(pContiguousMemoryInterface));
    mContiguousMemory = pContiguousMemoryInterface;
    mNativeType = pNativeType;
    mNumberOfChannels = pNumberOfChannels;
    mDimensions = pDimensions;
  }

  @Override
  public ClearCLContext getContext()
  {
    return null;
  }

  public ContiguousMemoryInterface getContiguousMemory()
  {
    return mContiguousMemory;
  }

  @Override
  public HostAccessType getHostAccessType()
  {
    return HostAccessType.ReadWrite;
  }

  @Override
  public long[] getDimensions()
  {
    return mDimensions;
  }

  @Override
  public NativeTypeEnum getNativeType()
  {
    return mNativeType;
  }

  @Override
  public long getPixelSizeInBytes()
  {
    return mNativeType.getSizeInBytes();
  }

  @Override
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
