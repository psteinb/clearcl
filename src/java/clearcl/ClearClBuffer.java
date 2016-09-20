package clearcl;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.DataType;
import coremem.ContiguousMemoryInterface;

public class ClearCLBuffer extends ClearCLBase
{

	private final ClearCLContext mClearCLContext;
	private final HostAccessType mHostAccessType;
	private final KernelAccessType mKernelAccessType;
	private final DataType mDataType;
	private final long mLength;

	public ClearCLBuffer(	ClearCLContext pClearCLContext,
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

	public void fill(	byte[] pPattern,
										long pOffsetInBuffer,
										long pLengthInBuffer,
										boolean pBlockingFill)
	{

		getBackend().enqueueFillBuffer(	mClearCLContext.getDefaultQueue()
																										.getPeerPointer(),
																		getPeerPointer(),
																		pBlockingFill,
																		pOffsetInBuffer * getDataType().getSizeInBytes(),
																		pLengthInBuffer * getDataType().getSizeInBytes(),
																		pPattern);
	}

	public void copyTo(	ClearCLBuffer pDstBuffer,
											long pOffsetInSrcBuffer,
											long pOffsetInDstBuffer,
											long pLengthInElements,
											boolean pBlockingCopy)
	{
		getBackend().enqueueCopyBuffer(	mClearCLContext.getDefaultQueue()
																										.getPeerPointer(),
																		getPeerPointer(),
																		pDstBuffer.getPeerPointer(),
																		pBlockingCopy,
																		pOffsetInSrcBuffer * getDataType().getSizeInBytes(),
																		pOffsetInDstBuffer * getDataType().getSizeInBytes(),
																		pLengthInElements * getDataType().getSizeInBytes());
	}

	public void copyTo(	ClearCLBuffer pDstBuffer,
											long[] pOriginInSrcBuffer,
											long[] pOriginInDstBuffer,
											long[] pRegion,
											boolean pBlockingCopy)
	{
		getBackend().enqueueCopyBufferBox(mClearCLContext.getDefaultQueue()
																											.getPeerPointer(),
																			getPeerPointer(),
																			pDstBuffer.getPeerPointer(),
																			pBlockingCopy,
																			pOriginInSrcBuffer,
																			pOriginInDstBuffer,
																			pRegion);
	}

	public void copyTo(	ClearCLImage pDstImage,
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

	public void writeTo(ContiguousMemoryInterface pContiguousMemory,
											long pOffsetInBuffer,
											long pLengthInBuffer,
											boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

		getBackend().enqueueReadFromBuffer(	mClearCLContext.getDefaultQueue()
																												.getPeerPointer(),
																				getPeerPointer(),
																				pBlockingRead,
																				pOffsetInBuffer * getDataType().getSizeInBytes(),
																				pLengthInBuffer * getDataType().getSizeInBytes(),
																				lHostMemPointer);
	}

	public void writeTo(Buffer pBuffer,
											long pOffsetInBuffer,
											long pLengthInBuffer,
											boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

		getBackend().enqueueReadFromBuffer(	mClearCLContext.getDefaultQueue()
																												.getPeerPointer(),
																				getPeerPointer(),
																				pBlockingRead,
																				pOffsetInBuffer * getDataType().getSizeInBytes(),
																				pLengthInBuffer * getDataType().getSizeInBytes(),
																				lHostMemPointer);
	}

	public void readFrom(	ContiguousMemoryInterface pContiguousMemory,
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

	public void readFrom(	Buffer pBuffer,
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

	public void writeTo(ContiguousMemoryInterface pContiguousMemory,
											long[] pBufferOrigin,
											long[] pHostOrigin,
											long[] Region,
											boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

		getBackend().enqueueReadFromBufferBox(mClearCLContext.getDefaultQueue()
																													.getPeerPointer(),
																					getPeerPointer(),
																					pBlockingRead,
																					pBufferOrigin,
																					pHostOrigin,
																					Region,
																					lHostMemPointer);
	}

	public void writeTo(Buffer pBuffer,
											long[] pBufferOrigin,
											long[] pHostOrigin,
											long[] Region,
											boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

		getBackend().enqueueReadFromBufferBox(mClearCLContext.getDefaultQueue()
																													.getPeerPointer(),
																					getPeerPointer(),
																					pBlockingRead,
																					pBufferOrigin,
																					pHostOrigin,
																					Region,
																					lHostMemPointer);
	}

	public void readFrom(	ContiguousMemoryInterface pContiguousMemory,
												long[] pBufferOrigin,
												long[] pHostOrigin,
												long[] Region,
												boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

		getBackend().enqueueWriteToBufferBox(	mClearCLContext.getDefaultQueue()
																													.getPeerPointer(),
																					getPeerPointer(),
																					pBlockingRead,
																					pBufferOrigin,
																					pHostOrigin,
																					Region,
																					lHostMemPointer);
	}

	public void readFrom(	Buffer pBuffer,
												long[] pBufferOrigin,
												long[] pHostOrigin,
												long[] Region,
												boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

		getBackend().enqueueWriteToBufferBox(	mClearCLContext.getDefaultQueue()
																													.getPeerPointer(),
																					getPeerPointer(),
																					pBlockingRead,
																					pBufferOrigin,
																					pHostOrigin,
																					Region,
																					lHostMemPointer);
	}

	public HostAccessType getHostAccessType()
	{
		return mHostAccessType;
	}

	public KernelAccessType getKernelAccessType()
	{
		return mKernelAccessType;
	}

	public DataType getDataType()
	{
		return mDataType;
	}

	public long getLengthInElements()
	{
		return mLength;
	}

	public long getSizeInBytes()
	{
		return getLengthInElements() * mDataType.getSizeInBytes();
	}

	@Override
	public String toString()
	{
		return String.format(	"ClearCLBuffer [mBufferType=%s, mDataType=%s, getLengthInElements()=%s, getSizeInBytes()=%s]",
													mHostAccessType,
													mDataType,
													getLengthInElements(),
													getSizeInBytes());
	}

	@Override
	public void close() throws Exception
	{
		getBackend().releaseBuffer(getPeerPointer());
	}

}
