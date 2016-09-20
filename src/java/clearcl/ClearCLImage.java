package clearcl;

import java.nio.Buffer;

import clearcl.enums.DataType;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageChannelType;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import coremem.ContiguousMemoryInterface;

public class ClearCLImage extends ClearCLBase
{
	private final ClearCLContext mClearCLContext;
	private final HostAccessType mHostAccessType;
	private final KernelAccessType mKernelAccessType;
	private final ImageType mImageType;
	private final ImageChannelOrder mImageChannelOrder;
	private final ImageChannelType mImageChannelType;
	private final long mWidth, mHeight, mDepth;

	public ClearCLImage(ClearCLContext pClearCLContext,
											ClearCLPeerPointer pImage,
											HostAccessType pHostAccessType,
											KernelAccessType pKernelAccessType,
											ImageType pImageType,
											ImageChannelOrder pImageChannelOrder,
											ImageChannelType pImageChannelType,
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
		mImageChannelType = pImageChannelType;
		mWidth = pWidth;
		mHeight = pHeight;
		mDepth = pDepth;
	}

	public void fill(	byte[] pPattern,
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

	public void copyTo(	ClearCLImage pDstImage,
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

	public void copyTo(	ClearCLBuffer pDstBuffer,
											long[] pOriginInSrcImage,
											long[] pRegionInDstImage,
											long pOffsetInDstBuffer,
											boolean pBlockingCopy)
	{
		getBackend().enqueueCopyImageToBuffer(mClearCLContext.getDefaultQueue()
																													.getPeerPointer(),
																					getPeerPointer(),
																					pDstBuffer.getPeerPointer(),
																					pBlockingCopy,
																					pOriginInSrcImage,
																					pRegionInDstImage,
																					pOffsetInDstBuffer);
	}

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

	public void readFrom(	ContiguousMemoryInterface pContiguousMemory,
												long[] pOrigin,
												long[] pRegion,
												boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pContiguousMemory);

		getBackend().enqueueWriteToImage(	mClearCLContext.getDefaultQueue()
																											.getPeerPointer(),
																			getPeerPointer(),
																			pBlockingRead,
																			pOrigin,
																			pRegion,
																			lHostMemPointer);
	}

	public void readFrom(	Buffer pBuffer,
												long[] pOrigin,
												long[] pRegion,
												boolean pBlockingRead)
	{
		ClearCLPeerPointer lHostMemPointer = getBackend().wrap(pBuffer);

		getBackend().enqueueWriteToImage(	mClearCLContext.getDefaultQueue()
																											.getPeerPointer(),
																			getPeerPointer(),
																			pBlockingRead,
																			pOrigin,
																			pRegion,
																			lHostMemPointer);
	}

	@Override
	public void close() throws Exception
	{
		getBackend().releaseImage(getPeerPointer());
	}

	public HostAccessType getHostAccessType()
	{
		return mHostAccessType;
	}

	public KernelAccessType getKernelAccessType()
	{
		return mKernelAccessType;
	}

	public ImageType getImageType()
	{
		return mImageType;
	}

	public ImageChannelOrder getImageChannelOrder()
	{
		return mImageChannelOrder;
	}

	public ImageChannelType getImageChannelType()
	{
		return mImageChannelType;
	}

	public DataType getDataType()
	{
		return mImageChannelType.getDataType();
	}

	public boolean isNormalized()
	{
		return mImageChannelType.isNormalized();
	}

	public long getWidth()
	{
		return mWidth;
	}

	public long getHeight()
	{
		return mHeight;
	}

	public long getDepth()
	{
		return mDepth;
	}

}
