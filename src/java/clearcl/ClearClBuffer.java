package clearcl;

import java.nio.Buffer;

import clearcl.abs.ClearCLMemBase;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.exceptions.ClearCLHostAccessException;
import clearcl.interfaces.ClearCLMemInterface;
import clearcl.util.Region3;
import coremem.ContiguousMemoryInterface;
import coremem.types.NativeTypeEnum;

/**
 * ClearCLBuffer is the ClearCL abstraction for OpenCL buffers.
 * 
 * @author royer
 */
public class ClearCLBuffer extends ClearCLMemBase implements
                                                 ClearCLMemInterface
{

  private final ClearCLContext mClearCLContext;
  private final HostAccessType mHostAccessType;
  private final KernelAccessType mKernelAccessType;
  private final NativeTypeEnum mNativeType;
  private final long mLength;

  /**
   * This constructor is called internally from an OpenCl context.
   * 
   * @param pClearCLContext
   *          context
   * @param pBufferPointer
   *          buffer pointer
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pNativeType
   *          data type
   * @param pLength
   *          length
   */
  ClearCLBuffer(ClearCLContext pClearCLContext,
                ClearCLPeerPointer pBufferPointer,
                HostAccessType pHostAccessType,
                KernelAccessType pKernelAccessType,
                NativeTypeEnum pNativeType,
                long pLength)
  {
    super(pClearCLContext.getBackend(), pBufferPointer);
    mClearCLContext = pClearCLContext;
    mHostAccessType = pHostAccessType;
    mKernelAccessType = pKernelAccessType;
    mNativeType = pNativeType;
    mLength = pLength;
  }

  /**
   * Fills the buffer with a given byte pattern.
   * 
   * @param pPattern
   *          pattern as a sequence of bytes
   * @param pBlockingFill
   *          true -> blocking call, false -> asynchronous call
   */
  public void fill(byte[] pPattern, boolean pBlockingFill)
  {
    fill(pPattern, 0, getSizeInBytes(), pBlockingFill);
  }

  /**
   * Fills the buffer with a given byte pattern, from a given starting offset,
   * and for a certain length. This call can be required to block until
   * operation is finished.
   * 
   * @param pPattern
   *          pattern as a sequence of bytes
   * @param pOffsetInBuffer
   *          offset in buffer
   * @param pLengthInBuffer
   *          length in buffer
   * @param pBlockingFill
   *          true -> blocking call, false -> asynchronous call
   */
  public void fill(byte[] pPattern,
                   long pOffsetInBuffer,
                   long pLengthInBuffer,
                   boolean pBlockingFill)
  {

    getBackend().enqueueFillBuffer(mClearCLContext.getDefaultQueue()
                                                  .getPeerPointer(),
                                   getPeerPointer(),
                                   pBlockingFill,
                                   pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                   pLengthInBuffer * getNativeType().getSizeInBytes(),
                                   pPattern);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies this OpenCl buffer into another OpenCl buffer of same length.
   * 
   * @param pDstBuffer
   *          destination buffer
   * @param pBlockingCopy
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLBuffer pDstBuffer, boolean pBlockingCopy)
  {
    copyTo(pDstBuffer, 0, 0, getSizeInBytes(), pBlockingCopy);
  }

  /**
   * Copies a linear region of this OpenCl buffer into a linear region of same
   * length of another OpenCl buffer.
   * 
   * @param pDstBuffer
   *          destination buffer
   * @param pOffsetInSrcBuffer
   *          source buffer offset
   * @param pOffsetInDstBuffer
   *          destination buffer offset
   * @param pLengthInElements
   *          copy length
   * @param pBlockingCopy
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLBuffer pDstBuffer,
                     long pOffsetInSrcBuffer,
                     long pOffsetInDstBuffer,
                     long pLengthInElements,
                     boolean pBlockingCopy)
  {
    getBackend().enqueueCopyBuffer(mClearCLContext.getDefaultQueue()
                                                  .getPeerPointer(),
                                   getPeerPointer(),
                                   pDstBuffer.getPeerPointer(),
                                   pBlockingCopy,
                                   pOffsetInSrcBuffer * getNativeType().getSizeInBytes(),
                                   pOffsetInDstBuffer * getNativeType().getSizeInBytes(),
                                   pLengthInElements * getNativeType().getSizeInBytes());
    pDstBuffer.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies a 3D region of this OpenCl buffer into a 3D region of same
   * dimensions of another OpenCl buffer.
   * 
   * @param pDstBuffer
   *          destination buffer
   * @param pOriginInSrcBuffer
   *          source buffer origin
   * @param pOriginInDstBuffer
   *          destination buffer origin
   * @param pRegion
   *          region to copy
   * @param pBlockingCopy
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLBuffer pDstBuffer,
                     long[] pOriginInSrcBuffer,
                     long[] pOriginInDstBuffer,
                     long[] pRegion,
                     boolean pBlockingCopy)
  {
    getBackend().enqueueCopyBufferRegion(mClearCLContext.getDefaultQueue()
                                                        .getPeerPointer(),
                                         getPeerPointer(),
                                         pDstBuffer.getPeerPointer(),
                                         pBlockingCopy,
                                         Region3.origin(pOriginInSrcBuffer),
                                         Region3.origin(pOriginInDstBuffer),
                                         Region3.region(pRegion));
    pDstBuffer.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies this OpenCl buffer to an OpenCl image.
   * 
   * @param pDstImage
   *          destination image
   * @param pBlockingCopy
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLImage pDstImage, boolean pBlockingCopy)
  {
    copyTo(pDstImage,
           0,
           Region3.originZero(),
           Region3.region(pDstImage.getDimensions()),
           pBlockingCopy);
  }

  /**
   * Copies a 3D region of this OpenCl buffer into a 3D region of same
   * dimensions of an OpenCl image.
   * 
   * @param pDstImage
   *          destination image
   * @param pOffsetInSrcBuffer
   *          source buffer offset
   * @param pDstOrigin
   *          destination origin
   * @param pDstRegion
   *          destination region
   * @param pBlockingCopy
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLImage pDstImage,
                     long pOffsetInSrcBuffer,
                     long[] pDstOrigin,
                     long[] pDstRegion,
                     boolean pBlockingCopy)
  {
    getBackend().enqueueCopyBufferToImage(mClearCLContext.getDefaultQueue()
                                                         .getPeerPointer(),
                                          getPeerPointer(),
                                          pDstImage.getPeerPointer(),
                                          pBlockingCopy,
                                          pOffsetInSrcBuffer * getNativeType().getSizeInBytes(),
                                          Region3.origin(pDstOrigin),
                                          Region3.region(pDstRegion));
    pDstImage.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Copies this image into a host image.
   * 
   * @param pClearCLHostImage
   *          host image.
   * @param pBlockingCopy
   *          true -> blocking call, false -> asynchronous call
   */
  public void copyTo(ClearCLHostImage pClearCLHostImage,
                     boolean pBlockingCopy)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue()
                                                      .getPeerPointer(),
                                       getPeerPointer(),
                                       pBlockingCopy,
                                       0,
                                       getSizeInBytes(),
                                       getBackend().wrap(pClearCLHostImage.getContiguousMemory()));
    pClearCLHostImage.notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Writes the contents of this OpenCl buffer into CoreMem buffer.
   * 
   * @param pContiguousMemory
   *          destination CoreMem buffer
   * @param pBlockingWrite
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      boolean pBlockingWrite)
  {
    writeTo(pContiguousMemory, 0, getSizeInBytes(), pBlockingWrite);
  }

  /**
   * Writes the contents of this OpenCl buffer into a linear region of a CoreMem
   * buffer.
   * 
   * @param pContiguousMemory
   *          destination CoreMem buffer
   * @param pOffsetInBuffer
   *          offset in destination buffer
   * @param pLengthInBuffer
   *          length to write
   * @param pBlockingWrite
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      long pOffsetInBuffer,
                      long pLengthInBuffer,
                      boolean pBlockingWrite)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue()
                                                      .getPeerPointer(),
                                       getPeerPointer(),
                                       pBlockingWrite,
                                       pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                       pLengthInBuffer * getNativeType().getSizeInBytes(),
                                       lHostMemPointer);
  }

  /**
   * Writes a NIO buffer into this OpenCl buffer.
   * 
   * @param pBuffer
   *          destination NIO buffer
   * 
   * @param pBlockingWrite
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(Buffer pBuffer, boolean pBlockingWrite)
  {
    writeTo(pBuffer, 0, getSizeInBytes(), pBlockingWrite);
  }

  /**
   * Writes a linear region of a NIO buffer into this OpenCl buffer.
   * 
   * @param pBuffer
   *          destination NIO buffer
   * @param pOffsetInBuffer
   *          offset in destination buffer
   * @param pLengthInBuffer
   *          length to write
   * @param pBlockingWrite
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(Buffer pBuffer,
                      long pOffsetInBuffer,
                      long pLengthInBuffer,
                      boolean pBlockingWrite)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue()
                                                      .getPeerPointer(),
                                       getPeerPointer(),
                                       pBlockingWrite,
                                       pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                       pLengthInBuffer * getNativeType().getSizeInBytes(),
                                       lHostMemPointer);
  }

  /**
   * Reads from a linear region of a CoreMem buffer into this OpenCl buffer.
   * 
   * @param pContiguousMemory
   *          source NIO buffer
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(ContiguousMemoryInterface pContiguousMemory,
                       boolean pBlockingRead)
  {
    readFrom(pContiguousMemory, 0, getSizeInBytes(), pBlockingRead);
  }

  /**
   * Reads from a linear region of a CoreMem buffer into this OpenCl buffer.
   * 
   * @param pContiguousMemory
   *          source NIO buffer
   * @param pOffsetInBuffer
   *          offset in source buffer
   * @param pLengthInBuffer
   *          length to read
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(ContiguousMemoryInterface pContiguousMemory,
                       long pOffsetInBuffer,
                       long pLengthInBuffer,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueWriteToBuffer(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                      pLengthInBuffer * getNativeType().getSizeInBytes(),
                                      lHostMemPointer);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Reads from a linear region of a NIO buffer into this OpenCl buffer.
   * 
   * @param pBuffer
   *          source NIO buffer
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(Buffer pBuffer, boolean pBlockingRead)
  {
    readFrom(pBuffer, 0, getSizeInBytes(), pBlockingRead);
  }

  /**
   * Reads from a linear region of a NIO buffer into this OpenCl buffer.
   * 
   * @param pBuffer
   *          source NIO buffer
   * @param pOffsetInBuffer
   *          offset in source buffer
   * @param pLengthInBuffer
   *          length to read
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(Buffer pBuffer,
                       long pOffsetInBuffer,
                       long pLengthInBuffer,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueWriteToBuffer(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      pOffsetInBuffer * getNativeType().getSizeInBytes(),
                                      pLengthInBuffer * getNativeType().getSizeInBytes(),
                                      lHostMemPointer);
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Writes to a 3D region of a CoreMem buffer into a 3D region of this OpenCl
   * buffer.
   * 
   * @param pContiguousMemory
   *          destination CoreMem buffer
   * @param pSourceOrigin
   *          origin in destination buffer
   * @param pDestinationOrigin
   *          origin in source buffer
   * @param Region
   *          region to write
   * @param pBlockingWrite
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      long[] pSourceOrigin,
                      long[] pDestinationOrigin,
                      long[] pRegion,
                      boolean pBlockingWrite)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueReadFromBufferRegion(mClearCLContext.getDefaultQueue()
                                                            .getPeerPointer(),
                                             getPeerPointer(),
                                             pBlockingWrite,
                                             Region3.origin(pSourceOrigin),
                                             Region3.origin(pDestinationOrigin),
                                             Region3.region(pRegion),
                                             lHostMemPointer);
  }

  /**
   * Writes to a 3D region of a NIO buffer into a 3D region of this OpenCl
   * buffer.
   * 
   * @param pBuffer
   *          destination NIO buffer
   * @param pSourceOrigin
   *          origin in source buffer
   * @param pDestinationOrigin
   *          origin in destination buffer
   * @param Region
   *          region to write
   * @param pBlockingWrite
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(Buffer pBuffer,
                      long[] pSourceOrigin,
                      long[] pDestinationOrigin,
                      long[] pRegion,
                      boolean pBlockingWrite)
  {
    if (!getHostAccessType().isReadableFromHost())
      throw new ClearCLHostAccessException("Image not readable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueReadFromBufferRegion(mClearCLContext.getDefaultQueue()
                                                            .getPeerPointer(),
                                             getPeerPointer(),
                                             pBlockingWrite,
                                             Region3.origin(pSourceOrigin),
                                             Region3.origin(pDestinationOrigin),
                                             Region3.region(pRegion),
                                             lHostMemPointer);
  }

  /**
   * Reads from a 3D region of a CoreMem buffer into a 3D region of this OpenCl
   * buffer.
   * 
   * @param pContiguousMemory
   *          source CoreMem buffer
   * @param pSourceOrigin
   *          origin in source buffer
   * @param pDestinationOrigin
   *          origin in destination buffer
   * @param pRegion
   *          region to read
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(ContiguousMemoryInterface pContiguousMemory,
                       long[] pSourceOrigin,
                       long[] pDestinationOrigin,
                       long[] pRegion,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueWriteToBufferRegion(mClearCLContext.getDefaultQueue()
                                                           .getPeerPointer(),
                                            getPeerPointer(),
                                            pBlockingRead,
                                            Region3.origin(pDestinationOrigin),
                                            Region3.origin(pSourceOrigin),
                                            Region3.region(pRegion),
                                            lHostMemPointer);

    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Reads from a 3D region of a NIO buffer into a 3D region of this OpenCl
   * buffer.
   * 
   * @param pBuffer
   *          source NIO buffer
   * @param pSourceOrigin
   *          origin in source buffer
   * @param pDestinationOrigin
   *          origin in destination buffer
   * @param pRegion
   *          region to read
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void readFrom(Buffer pBuffer,
                       long[] pSourceOrigin,
                       long[] pDestinationOrigin,
                       long[] pRegion,
                       boolean pBlockingRead)
  {
    if (!getHostAccessType().isWritableFromHost())
      throw new ClearCLHostAccessException("Image not writable from host");

    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueWriteToBufferRegion(mClearCLContext.getDefaultQueue()
                                                           .getPeerPointer(),
                                            getPeerPointer(),
                                            pBlockingRead,
                                            Region3.origin(pDestinationOrigin),
                                            Region3.origin(pSourceOrigin),
                                            Region3.region(pRegion),
                                            lHostMemPointer);
  
    notifyListenersOfChange(mClearCLContext.getDefaultQueue());
  }

  /**
   * Returns host access type of this buffer.
   * 
   * @return host access type
   */
  public HostAccessType getHostAccessType()
  {
    return mHostAccessType;
  }

  /**
   * Returns kernel access type of this buffer.
   * 
   * @return kernel access type
   */
  public KernelAccessType getKernelAccessType()
  {
    return mKernelAccessType;
  }

  /**
   * Returns data type.
   * 
   * @return data type
   */
  public NativeTypeEnum getNativeType()
  {
    return mNativeType;
  }

  /**
   * Returns length in elements (not necessarily equal to size bytes!)
   * 
   * @return length in elements
   */
  public long getLengthInElements()
  {
    return mLength;
  }

  /**
   * Returns the size in bytes.
   * 
   * @return size in bytes
   */
  @Override
  public long getSizeInBytes()
  {
    return getLengthInElements() * mNativeType.getSizeInBytes();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return String.format("ClearCLBuffer [mBufferType=%s, mDataType=%s, getLengthInElements()=%s, getSizeInBytes()=%s]",
                         mHostAccessType,
                         mNativeType,
                         getLengthInElements(),
                         getSizeInBytes());
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close()
  {
    getBackend().releaseBuffer(getPeerPointer());
  }

}
