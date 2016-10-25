package clearcl;

import java.nio.Buffer;

import clearcl.abs.ClearCLMemBase;
import clearcl.enums.DataType;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.interfaces.ClearCLMemInterface;
import coremem.ContiguousMemoryInterface;

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
  private final DataType mDataType;
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
   * @param pDataType
   *          data type
   * @param pLength
   *          length
   */
  ClearCLBuffer(ClearCLContext pClearCLContext,
                ClearCLPeerPointer pBufferPointer,
                HostAccessType pHostAccessType,
                KernelAccessType pKernelAccessType,
                DataType pDataType,
                long pLength)
  {
    super(pClearCLContext.getBackend(), pBufferPointer);
    mClearCLContext = pClearCLContext;
    mHostAccessType = pHostAccessType;
    mKernelAccessType = pKernelAccessType;
    mDataType = pDataType;
    mLength = pLength;
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
                                   pOffsetInBuffer * getDataType().getSizeInBytes(),
                                   pLengthInBuffer * getDataType().getSizeInBytes(),
                                   pPattern);
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
                                   pOffsetInSrcBuffer * getDataType().getSizeInBytes(),
                                   pOffsetInDstBuffer * getDataType().getSizeInBytes(),
                                   pLengthInElements * getDataType().getSizeInBytes());
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
                                         pOriginInSrcBuffer,
                                         pOriginInDstBuffer,
                                         pRegion);
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
                                          pOffsetInSrcBuffer * getDataType().getSizeInBytes(),
                                          pDstOrigin,
                                          pDstRegion);
  }

  /**
   * Writes a linear region of a CoreMem buffer into this OpenCl buffer.
   * 
   * @param pContiguousMemory
   *          destination CoreMem buffer
   * @param pOffsetInBuffer
   *          offset in destination buffer
   * @param pLengthInBuffer
   *          length to write
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      long pOffsetInBuffer,
                      long pLengthInBuffer,
                      boolean pBlockingRead)
  {
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue()
                                                      .getPeerPointer(),
                                       getPeerPointer(),
                                       pBlockingRead,
                                       pOffsetInBuffer * getDataType().getSizeInBytes(),
                                       pLengthInBuffer * getDataType().getSizeInBytes(),
                                       lHostMemPointer);
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
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(Buffer pBuffer,
                      long pOffsetInBuffer,
                      long pLengthInBuffer,
                      boolean pBlockingRead)
  {
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueReadFromBuffer(mClearCLContext.getDefaultQueue()
                                                      .getPeerPointer(),
                                       getPeerPointer(),
                                       pBlockingRead,
                                       pOffsetInBuffer * getDataType().getSizeInBytes(),
                                       pLengthInBuffer * getDataType().getSizeInBytes(),
                                       lHostMemPointer);
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
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueWriteToBuffer(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      pOffsetInBuffer * getDataType().getSizeInBytes(),
                                      pLengthInBuffer * getDataType().getSizeInBytes(),
                                      lHostMemPointer);
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
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueWriteToBuffer(mClearCLContext.getDefaultQueue()
                                                     .getPeerPointer(),
                                      getPeerPointer(),
                                      pBlockingRead,
                                      pOffsetInBuffer * getDataType().getSizeInBytes(),
                                      pLengthInBuffer * getDataType().getSizeInBytes(),
                                      lHostMemPointer);
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
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      long[] pSourceOrigin,
                      long[] pDestinationOrigin,
                      long[] pRegion,
                      boolean pBlockingRead)
  {
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueReadFromBufferBox(mClearCLContext.getDefaultQueue()
                                                         .getPeerPointer(),
                                          getPeerPointer(),
                                          pBlockingRead,
                                          pSourceOrigin,
                                          pDestinationOrigin,
                                          pRegion,
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
   * @param pBlockingRead
   *          true -> blocking call, false -> asynchronous call
   */
  public void writeTo(Buffer pBuffer,
                      long[] pSourceOrigin,
                      long[] pDestinationOrigin,
                      long[] pRegion,
                      boolean pBlockingRead)
  {
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueReadFromBufferBox(mClearCLContext.getDefaultQueue()
                                                         .getPeerPointer(),
                                          getPeerPointer(),
                                          pBlockingRead,
                                          pSourceOrigin,
                                          pDestinationOrigin,
                                          pRegion,
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
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

    getBackend().enqueueWriteToBufferRegion(mClearCLContext.getDefaultQueue()
                                                           .getPeerPointer(),
                                            getPeerPointer(),
                                            pBlockingRead,
                                            pDestinationOrigin,
                                            pSourceOrigin,
                                            pRegion,
                                            lHostMemPointer);
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
    ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

    getBackend().enqueueWriteToBufferRegion(mClearCLContext.getDefaultQueue()
                                                           .getPeerPointer(),
                                            getPeerPointer(),
                                            pBlockingRead,
                                            pDestinationOrigin,
                                            pSourceOrigin,
                                            pRegion,
                                            lHostMemPointer);
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
  public DataType getDataType()
  {
    return mDataType;
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
    return getLengthInElements() * mDataType.getSizeInBytes();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return String.format("ClearCLBuffer [mBufferType=%s, mDataType=%s, getLengthInElements()=%s, getSizeInBytes()=%s]",
                         mHostAccessType,
                         mDataType,
                         getLengthInElements(),
                         getSizeInBytes());
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close() throws Exception
  {
    getBackend().releaseBuffer(getPeerPointer());
  }

}
