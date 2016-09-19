package clearcl;

import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageChannelType;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;

public class ClearCLImage extends ClearCLBase
{

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
		mHostAccessType = pHostAccessType;
		mKernelAccessType = pKernelAccessType;
		mImageType = pImageType;
		mImageChannelOrder = pImageChannelOrder;
		mImageChannelType = pImageChannelType;
		mWidth = pWidth;
		mHeight = pHeight;
		mDepth = pDepth;
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
