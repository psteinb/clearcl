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

/**
 * ClearCLHostImageBuffer is the ClearCL abstraction for CPU RAM images.
 *
 * @author royer
 */
public class ClearCLHostImageBuffer extends ClearCLMemBase implements
                                    ClearCLMemInterface,
                                    ClearCLImageInterface
{

  private ContiguousMemoryInterface mContiguousMemory;
  private NativeTypeEnum mNativeType;
  private final long[] mDimensions;
  private long mNumberOfChannels;

  /**
   * Allocates a host image buffer of same dimensions than a given image.
   * 
   * @param pClearCLImage
   * @return
   */
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

  /**
   * Internal method to allocate offheap memory.
   * 
   * @param pSizeInBytes
   * @return
   */
  private static OffHeapMemory allocate(long pSizeInBytes)
  {
    return OffHeapMemory.allocatePageAlignedBytes("ClearCLHostImageBuffer",
                                                  pSizeInBytes);
  }

  /**
   * Creates a host image buffer from a context, native type, number of
   * channels, and dimensions.
   * 
   * @param pClearCLContext
   * @param pNativeType
   * @param pNumberOfChannels
   * @param pDimensions
   */
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

  /**
   * Creates a host image buffer from a context, an existing contiguous memory
   * object, native type, number of channels, and dimensions.
   * 
   * @param pClearCLContext
   * @param pContiguousMemoryInterface
   * @param pNativeType
   * @param pNumberOfChannels
   * @param pDimensions
   */
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

  /**
   * Returns the contiguous memory object used to store the data.
   * @return
   */
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

  private static final long getVolume(long[] pDimensions)
  {
    long lVolume = 1;
    for (int i = 0; i < pDimensions.length; i++)
      lVolume *= pDimensions[i];
    return lVolume;
  }


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
    mContiguousMemory = null;
  }

}
