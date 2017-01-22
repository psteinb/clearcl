package clearcl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import clearcl.abs.ClearCLMemBase;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.exceptions.ClearCLException;
import clearcl.exceptions.ClearCLHostAccessException;
import clearcl.exceptions.ClearCLIllegalArgumentException;
import clearcl.interfaces.ClearCLImageInterface;
import clearcl.interfaces.ClearCLMemInterface;
import clearcl.util.Region3;
import coremem.ContiguousMemoryInterface;
import coremem.buffers.ContiguousBuffer;
import coremem.enums.NativeTypeEnum;
import coremem.fragmented.FragmentedMemoryInterface;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;

/**
 * ClearCLImage is the ClearCL abstraction for OpenCL images.
 *
 * @author royer
 */
public class ClearCLImage extends ClearCLMemBase implements
                          ClearCLMemInterface,
                          ClearCLImageInterface
{
  private final ClearCLContext mClearCLContext;
  private final HostAccessType mHostAccessType;
  private final KernelAccessType mKernelAccessType;
  private final ImageType mImageType;
  private final ImageChannelOrder mImageChannelOrder;
  private final ImageChannelDataType mImageChannelDataType;
  private final long[] mDimensions;

  /**
   * This constructor is called internally from an OpenCl context.
   * 
   * @param pClearCLContext
   *          context
   * @param pImage
   *          image peer pointer
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pImageType
   *          image type (1D, 2D, or 3D)
   * @param pImageChannelOrder
   *          channel order
   * @param pImageChannelType
   *          channel data type
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   */
  ClearCLImage(ClearCLContext pClearCLContext,
               ClearCLPeerPointer pImage,
               HostAccessType pHostAccessType,
               KernelAccessType pKernelAccessType,
               ImageType pImageType,
               ImageChannelOrder pImageChannelOrder,
               ImageChannelDataType pImageChannelType,
               long... pDimensions)
  {
    super(pClearCLContext.getBackend(), pImage);
    mClearCLContext = pClearCLContext;
    mHostAccessType = pHostAccessType;
    mKernelAccessType = pKernelAccessType;
    mImageType = pImageType;
    mImageChannelOrder = pImageChannelOrder;
    mImageChannelDataType = pImageChannelType;
    mDimensions = pDimensions;
  }

  /**
   * Fills this image with zeros.
   * 
   * @param pBlockingFill
   *          true -> blocking call, false -> asynchronous call
   */

  public void fillZero(boolean pBlockingFill)
  {
    fill(0.0f, pBlockingFill);
  }

  /**
   * Fills this image with a single channel 'color'.
   * 
   * @param pColor
   *          single channel float color
   * @param pBlockingFill
   *          true -> blocking call, false -> asynchronous call
   */
  public void fill(float pColor, boolean pBlockingFill)
  {
    fill(new float[]
    { pColor, pColor, pColor, pColor },
         Region3.originZero(),
         Region3.region(getDimensions()),
         pBlockingFill);
  }

  /**
   * Fills this image with a RGBA 'color'.
   * 
   * @param pRGBA
   *          four float color
   * @param pBlockingFill
   *          true -> blocking call, false -> asynchronous call
   */
  public void fill(float[] pRGBA, boolean pBlockingFill)
  {
    fill(pRGBA,
         Region3.originZero(),
         Region3.region(getDimensions()),
         pBlockingFill);
  }

  /**
   * Fills a nD region of this image with a RGBA 'color'.
   * 
   * @param pRGBA
   *          four float color
   * @param pOrigin
   *          region origin
   * @param pRegion
   *          region dimensions
   * @param pBlockingFill
   *          true -> blocking call, false -> asynchronous call
   */
  public void fill(float[] pRGBA,
                   long[] pOrigin,
                   long[] pRegion,
                   boolean pBlockingFill)
  {
    if (!(isNormalized() || isFloat()))
      throw new ClearCLIllegalArgumentException("Image data type must not be normalized integer or float");
    if (pRGBA.length > 4)
      throw new ClearCLIllegalArgumentException("Float array must have length 4");

    int lLength = 4 * 4;
    ContiguousBuffer lContiguousBuffer =
                                       ContiguousBuffer.allocate(lLength);
    for (float lFloat : pRGBA)
      lContiguousBuffer.writeFloat(lFloat);
    lContiguousBuffer.rewind();

    byte[] lPattern = new byte[lLength];

    for (int i = 0; i < lLength; i++)
      lPattern[i] = lContiguousBuffer.readByte();

    getBackend().enqueueFillImage(mClearCLContext.getDefaultQueue()
                                                 .getPeerPointer(),
                                  getPeerPointer(),
                                  pBlockingFill,
                                  Region3.origin(pOrigin),
                                  Region3.region(pRegion),
                                  lPattern);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies this image to another image of same dimensions.
   * 
   * @param pDstImage
   *          destination image
   * @param pBlocking
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLImage pDstImage, boolean pBlockingCopy)
  {
    copyTo(pDstImage,
           Region3.originZero(),
           Region3.originZero(),
           Region3.region(getDimensions()),
           pBlockingCopy);
  }

  /**
   * Copies a nD region form this image to a nD region of same dimensions in
   * another image.
   * 
   * @param pDstImage
   *          destination image
   * @param pOriginInSrcImage
   *          origin in source image
   * @param pOriginInDstImage
   *          origin in destination image
   * @param pRegion
   *          region dimensions
   * @param pBlocking
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLImage pDstImage,
                     long[] pOriginInSrcImage,
                     long[] pOriginInDstImage,
                     long[] pRegion,
                     boolean pBlockingCopy)
  {
    getBackend().enqueueCopyImage(mClearCLContext.getDefaultQueue()
                                                 .getPeerPointer(),
                                  getPeerPointer(),
                                  pDstImage.getPeerPointer(),
                                  pBlockingCopy,
                                  pOriginInSrcImage,
                                  pOriginInDstImage,
                                  pRegion);
    pDstImage.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies this image into an (OpenCl) buffer.
   * 
   * @param pDstBuffer
   *          destination buffer
   * @param pBlockingCopy
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLBuffer pDstBuffer, boolean pBlockingCopy)
  {
    copyTo(pDstBuffer,
           Region3.originZero(),
           Region3.region(getDimensions()),
           0,
           pBlockingCopy);
  }

  /**
   * Copies a nD region from this image into a nD region of same dimensions into
   * a (OpenCl) buffer.
   * 
   * @param pDstBuffer
   *          destination buffer
   * @param pOriginInSrcImage
   *          origin is source image
   * @param pRegionInSrcImage
   *          region dimensions in source image
   * @param pOffsetInDstBuffer
   *          offset in destination buffer in elements
   * @param pBlockingCopy
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLBuffer pDstBuffer,
                     long[] pOriginInSrcImage,
                     long[] pRegionInSrcImage,
                     long pOffsetInDstBuffer,
                     boolean pBlockingCopy)
  {

    getBackend().enqueueCopyImageToBuffer(mClearCLContext.getDefaultQueue()
                                                         .getPeerPointer(),
                                          getPeerPointer(),
                                          pDstBuffer.getPeerPointer(),
                                          pBlockingCopy,
                                          Region3.origin(pOriginInSrcImage),
                                          Region3.region(pRegionInSrcImage),
                                          pOffsetInDstBuffer * pDstBuffer.getNativeType()
                                                                         .getSizeInBytes());
    pDstBuffer.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies this buffer into a host image.
   * 
   * @param pClearCLHostImage
   *          host image.
   * @param pBlockingCopy
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLHostImageBuffer pClearCLHostImage,
                     boolean pBlockingCopy)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Buffer not readable from host");

    if (pClearCLHostImage.getSizeInBytes() != getSizeInBytes())
      throw new ClearCLException("Incompatible length");

    getBackend().enqueueReadFromImage(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingCopy,
                                      Region3.originZero(),
                                      Region3.region(getDimensions()),
                                      getBackend().wrap(pClearCLHostImage.getContiguousMemory()));

    pClearCLHostImage.notifyListenersOfChange(mClearCLContext.getDefaultQueue());

  }

  /**
   * Writes a nD region of this image to a NIO buffer.
   * 
   * @param pBuffer
   *          NIO buffer
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(Buffer pBuffer, boolean pBlockingRead)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueReadFromImage(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      Region3.originZero(),
                                      Region3.region(getDimensions()),
                                      lHostMemPointer);
  }

  /**
   * Writes a nD region of this image to a CoreMem buffer.
   * 
   * @param pContiguousMemory
   *          CoreMem buffer
   * @param pOrigin
   *          origin in image
   * @param pRegion
   *          region dimensions in image
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      long[] pOrigin,
                      long[] pRegion,
                      boolean pBlockingRead)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    ClearCLPeerPointer lHostMemPointer =
                                       getBackend().wrap(pContiguousMemory);

    getBackend().enqueueReadFromImage(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      Region3.origin(pOrigin),
                                      Region3.region(pRegion),
                                      lHostMemPointer);
  }

  /**
   * Writes a nD region of this image to a NIO buffer.
   * 
   * @param pBuffer
   *          NIO buffer
   * @param pOrigin
   *          origin in image
   * @param pRegion
   *          region dimensions in image
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(Buffer pBuffer,
                      long[] pOrigin,
                      long[] pRegion,
                      boolean pBlockingRead)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueReadFromImage(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      Region3.origin(pOrigin),
                                      Region3.region(pRegion),
                                      lHostMemPointer);
  }

  /**
   * Reads from a CoreMem buffer into a nD region of this image.
   * 
   * @param pContiguousMemory
   *          CoreMem buffer
   * @param pOrigin
   *          origin in image
   * @param pRegion
   *          region dimensions in image
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(ContiguousMemoryInterface pContiguousMemory,
                       long[] pOrigin,
                       long[] pRegion,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    ClearCLPeerPointer lHostMemPointer =
                                       getBackend().wrap(pContiguousMemory);

    getBackend().enqueueWriteToImage(mClearCLContext.getDefaultQueue()
                                                    .getPeerPointer(),
                                     getPeerPointer(),
                                     pBlockingRead,
                                     Region3.origin(pOrigin),
                                     Region3.region(pRegion),
                                     lHostMemPointer);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Reads from a CoreMem contiguous buffer into this image.
   * 
   * @param pContiguousMemory
   *          CoreMem buffer
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(ContiguousMemoryInterface pContiguousMemory,
                       boolean pBlockingRead)
  {
    readFrom(pContiguousMemory,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);
  }

  /**
   * Reads from a CoreMem fragmented buffer into a nD region of this image.
   * 
   * @param pFragmentedMemory
   *          CoreMem fragmented buffer
   * @param pOrigin
   *          origin in image
   * @param pRegion
   *          region dimensions in image
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(FragmentedMemoryInterface pFragmentedMemory,
                       long[] pOrigin,
                       long[] pRegion,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    ClearCLPeerPointer lHostMemPointer =
                                       getBackend().wrap(pFragmentedMemory);

    getBackend().enqueueWriteToImage(mClearCLContext.getDefaultQueue()
                                                    .getPeerPointer(),
                                     getPeerPointer(),
                                     pBlockingRead,
                                     Region3.origin(pOrigin),
                                     Region3.region(pRegion),
                                     lHostMemPointer);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Reads from a CoreMem fragmented buffer into this image.
   * 
   * @param pFragmentedMemory
   *          CoreMem fragmented buffer
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(FragmentedMemoryInterface pFragmentedMemory,
                       boolean pBlockingRead)
  {
    readFrom(pFragmentedMemory,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);
  }

  /**
   * Reads from a NIO buffer into a nD region of this image.
   * 
   * @param pBuffer
   *          NIO buffer
   * @param pOrigin
   *          origin in image
   * @param pRegion
   *          region dimensions in image
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(Buffer pBuffer,
                       long[] pOrigin,
                       long[] pRegion,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueWriteToImage(mClearCLContext.getDefaultQueue()
                                                    .getPeerPointer(),
                                     getPeerPointer(),
                                     pBlockingRead,
                                     Region3.origin(pOrigin),
                                     Region3.region(pRegion),
                                     lHostMemPointer);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Reads from a NIO buffer into this image.
   * 
   * @param pBuffer
   *          NIO buffer
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(Buffer pBuffer, boolean pBlockingRead)
  {
    readFrom(pBuffer,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);
  }

  /**
   * Reads from a Java byte array into this image.
   * 
   * @param Java
   *          byte array
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   * @return
   */
  public OffHeapMemory readFrom(byte[] pByteArray,
                                boolean pBlockingRead)
  {
    OffHeapMemory lOffHeapMemory =
                                 OffHeapMemory.allocateBytes(pByteArray.length);

    lOffHeapMemory.copyFrom(pByteArray);

    readFrom(lOffHeapMemory,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);

    return lOffHeapMemory;
  }

  /**
   * Reads from a Java char array into this image.
   * 
   * @param Java
   *          char array
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   * @return
   */
  public OffHeapMemory readFrom(char[] pCharArray,
                                boolean pBlockingRead)
  {
    OffHeapMemory lOffHeapMemory =
                                 OffHeapMemory.allocateChars(pCharArray.length);

    lOffHeapMemory.copyFrom(pCharArray);

    readFrom(lOffHeapMemory,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);

    return lOffHeapMemory;
  }

  /**
   * Reads from a Java short array into this image.
   * 
   * @param Java
   *          short array
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   * @return
   */
  public OffHeapMemory readFrom(short[] pShortsArray,
                                boolean pBlockingRead)
  {
    OffHeapMemory lOffHeapMemory =
                                 OffHeapMemory.allocateShorts(pShortsArray.length);

    lOffHeapMemory.copyFrom(pShortsArray);

    readFrom(lOffHeapMemory,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);

    return lOffHeapMemory;
  }

  /**
   * Reads from a Java int array into this image.
   * 
   * @param Java
   *          int array
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   * @return
   */
  public OffHeapMemory readFrom(int[] pIntArray,
                                boolean pBlockingRead)
  {
    OffHeapMemory lOffHeapMemory =
                                 OffHeapMemory.allocateInts(pIntArray.length);

    lOffHeapMemory.copyFrom(pIntArray);

    readFrom(lOffHeapMemory,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);

    return lOffHeapMemory;
  }

  /**
   * Reads from a Java long array into this image.
   * 
   * @param Java
   *          long array
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   * @return
   */
  public OffHeapMemory readFrom(long[] pLongArray,
                                boolean pBlockingRead)
  {
    OffHeapMemory lOffHeapMemory =
                                 OffHeapMemory.allocateLongs(pLongArray.length);

    lOffHeapMemory.copyFrom(pLongArray);

    readFrom(lOffHeapMemory,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);

    return lOffHeapMemory;
  }

  /**
   * Reads from a Java float array into this image.
   * 
   * @param Java
   *          float array
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   * @return
   */
  public OffHeapMemory readFrom(float[] pFloatArray,
                                boolean pBlockingRead)
  {
    OffHeapMemory lOffHeapMemory =
                                 OffHeapMemory.allocateFloats(pFloatArray.length);

    lOffHeapMemory.copyFrom(pFloatArray);

    readFrom(lOffHeapMemory,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);

    return lOffHeapMemory;
  }

  /**
   * Reads from a Java double array into this image.
   * 
   * @param Java
   *          double array
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   * @return
   */
  public OffHeapMemory readFrom(double[] pDoubleArray,
                                boolean pBlockingRead)
  {
    OffHeapMemory lOffHeapMemory =
                                 OffHeapMemory.allocateDoubles(pDoubleArray.length);

    lOffHeapMemory.copyFrom(pDoubleArray);

    readFrom(lOffHeapMemory,
             Region3.originZero(),
             Region3.region(getDimensions()),
             pBlockingRead);

    return lOffHeapMemory;
  }

  /**
   * Returns the context for this image.
   * 
   * @return context
   */
  @Override
  public ClearCLContext getContext()
  {
    return mClearCLContext;
  }

  /**
   * Returns host access type
   * 
   * @return host acess type
   */
  @Override
  public HostAccessType getHostAccessType()
  {
    return mHostAccessType;
  }

  /**
   * Returns kernel access type
   * 
   * @return kernel access type
   */
  public KernelAccessType getKernelAccessType()
  {
    return mKernelAccessType;
  }

  /**
   * Returns image type
   * 
   * @return image type
   */
  public ImageType getImageType()
  {
    return mImageType;
  }

  /**
   * Returns image type
   * 
   * @return image type
   */
  public ImageChannelOrder getChannelOrder()
  {
    return mImageChannelOrder;
  }

  /**
   * Returns image channel type
   * 
   * @return channel type
   */
  public ImageChannelDataType getChannelDataType()
  {
    return mImageChannelDataType;
  }

  /**
   * Returns whether this image is normalized (values between 0 and 1)
   * 
   * @return true if normalized, false otherwise
   */
  public boolean isNormalized()
  {
    return mImageChannelDataType.isNormalized();
  }
  
  /**
   * Returns whether this image data type has a sign
   * 
   * @return true if signed, false otherwise
   */
  public boolean isSigned()
  {
    return mImageChannelDataType.isSigned();
  }
  
  /**
   * Returns whether this image data type is integer
   * 
   * @return true if integer, false otherwise
   */
  public boolean isInteger()
  {
    return mImageChannelDataType.isInteger();
  }
  
  /**
   * Returns whether this image data type is float
   * 
   * @return true if float, false otherwise
   */
  public boolean isFloat()
  {
    return mImageChannelDataType.isFloat();
  }

  @Override
  public NativeTypeEnum getNativeType()
  {
    return getChannelDataType().getNativeType();
  }

  @Override
  public long getNumberOfChannels()
  {
    return getChannelOrder().getNumberOfChannels();
  }

  /**
   * Returns this image dimensions.
   * 
   * @return dimensions
   */
  @Override
  public long[] getDimensions()
  {
    return Arrays.copyOf(mDimensions, mDimensions.length);
  }

  /* (non-Javadoc)
   * @see coremem.interfaces.SizedInBytes#getSizeInBytes()
   */
  @Override
  public long getSizeInBytes()
  {
    return getVolume()
           * getChannelDataType().getNativeType().getSizeInBytes()
           * getChannelOrder().getNumberOfChannels();
  }

  @Override
  public String toString()
  {
    return String.format("ClearCLImage [getHostAccessType()=%s, getKernelAccessType()=%s, getImageType()=%s, getChannelOrder()=%s, getChannelDataType()=%s, isNormalized()=%s, getNativeType()=%s, getNumberOfChannels()=%s, getDimensions()=%s, getSizeInBytes()=%s]",
                         getHostAccessType(),
                         getKernelAccessType(),
                         getImageType(),
                         getChannelOrder(),
                         getChannelDataType(),
                         isNormalized(),
                         getNativeType(),
                         getNumberOfChannels(),
                         Arrays.toString(getDimensions()),
                         getSizeInBytes());
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close()
  {
    getBackend().releaseImage(getPeerPointer());
    setPeerPointer(null);
  }

}
