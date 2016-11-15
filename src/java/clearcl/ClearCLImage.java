package clearcl;

import java.nio.Buffer;
import java.util.Arrays;

import clearcl.abs.ClearCLMemBase;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.exceptions.ClearCLHostAccessException;
import clearcl.interfaces.ClearCLImageInterface;
import clearcl.interfaces.ClearCLMemInterface;
import clearcl.util.Region3;
import coremem.ContiguousMemoryInterface;
import coremem.enums.NativeTypeEnum;

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
   * Fills this image with a byte pattern.
   * 
   * @param pPattern
   *          byte pattern
   * @param pBlockingFill
   *          true -> blocking call, false -> asynchronous call
   */
  public void fill(byte[] pPattern, boolean pBlockingFill)
  {
    fill(pPattern,
         Region3.originZero(),
         Region3.region(getDimensions()),
         pBlockingFill);
  }

  /**
   * Fills a nD region of this image with a byte pattern.
   * 
   * @param pPattern
   *          byte pattern
   * @param pOrigin
   *          region origin
   * @param pRegion
   *          region dimensions
   * @param pBlockingFill
   *          true -> blocking call, false -> asynchronous call
   */
  public void fill(byte[] pPattern,
                   long[] pOrigin,
                   long[] pRegion,
                   boolean pBlockingFill)
  {
    getBackend().enqueueFillImage(mClearCLContext.getDefaultQueue()
                                                 .getPeerPointer(),
                                  getPeerPointer(),
                                  pBlockingFill,
                                  Region3.origin(pOrigin),
                                  Region3.region(pRegion),
                                  pPattern);
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
   *          offset in destination buffer
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
                                          pOffsetInDstBuffer);
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

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

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

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

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
   * Reads from a CoreMem buffer into this image.
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
    return getVolume() * getChannelDataType().getNativeType()
                                             .getSizeInBytes()
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
  }

}
