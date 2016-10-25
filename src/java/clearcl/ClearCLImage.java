package clearcl;

import java.nio.Buffer;

import clearcl.abs.ClearCLMemBase;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.interfaces.ClearCLMemInterface;
import coremem.ContiguousMemoryInterface;

/**
 * ClearCLImage is the ClearCL abstraction for OpenCL images.
 *
 * @author royer
 */
public class ClearCLImage extends ClearCLMemBase implements
                                                ClearCLMemInterface
{
  private final ClearCLContext mClearCLContext;
  private final HostAccessType mHostAccessType;
  private final KernelAccessType mKernelAccessType;
  private final ImageType mImageType;
  private final ImageChannelOrder mImageChannelOrder;
  private final ImageChannelDataType mImageChannelDataType;
  private final long mWidth, mHeight, mDepth;

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
               long pWidth,
               long pHeight,
               long pDepth)
  {
    super(pClearCLContext.getBackend(), pImage);
    mClearCLContext = pClearCLContext;
    mHostAccessType = pHostAccessType;
    mKernelAccessType = pKernelAccessType;
    mImageType = pImageType;
    mImageChannelOrder = pImageChannelOrder;
    mImageChannelDataType = pImageChannelType;
    mWidth = pWidth;
    mHeight = pHeight;
    mDepth = pDepth;
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
                                  pOrigin,
                                  pRegion,
                                  pPattern);
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
                                          pOriginInSrcImage,
                                          pRegionInSrcImage,
                                          pOffsetInDstBuffer);
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
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueReadFromImage(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      pOrigin,
                                      pRegion,
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
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueReadFromImage(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      pOrigin,
                                      pRegion,
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
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueWriteToImage(mClearCLContext.getDefaultQueue()
                                                    .getPeerPointer(),
                                     getPeerPointer(),
                                     pBlockingRead,
                                     pOrigin,
                                     pRegion,
                                     lHostMemPointer);
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
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueWriteToImage(mClearCLContext.getDefaultQueue()
                                                    .getPeerPointer(),
                                     getPeerPointer(),
                                     pBlockingRead,
                                     pOrigin,
                                     pRegion,
                                     lHostMemPointer);
  }

  /**
   * Returns host access type
   * 
   * @return host acess type
   */
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
  public ImageChannelOrder getImageChannelOrder()
  {
    return mImageChannelOrder;
  }

  /**
   * Returns image channel type
   * 
   * @return channel type
   */
  public ImageChannelDataType getImageChannelDataType()
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
   * Returns this image width
   * 
   * @return width
   */
  public long getWidth()
  {
    return mWidth;
  }

  /**
   * Returns this image height
   * 
   * @return height
   */
  public long getHeight()
  {
    return mHeight;
  }

  /**
   * Returns this image depth
   * 
   * @return depth
   */
  public long getDepth()
  {
    return mDepth;
  }

  /**
   * Returns this image volume
   * 
   * @return depth
   */
  public long getVolume()
  {
    return mWidth * mHeight * mDepth;
  }

  @Override
  public long getSizeInBytes()
  {
    return getVolume() * getImageChannelDataType().getDataType()
                                                  .getSizeInBytes();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return String.format("ClearCLImage [mImageType=%s, mImageChannelOrder=%s, mImageChannelType=%s, mWidth=%s, mHeight=%s, mDepth=%s, mHostAccessType=%s, mKernelAccessType=%s]",
                         mImageType,
                         mImageChannelOrder,
                         mImageChannelDataType,
                         mWidth,
                         mHeight,
                         mDepth,
                         mHostAccessType,
                         mKernelAccessType);
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close() throws Exception
  {
    getBackend().releaseImage(getPeerPointer());
  }

}
