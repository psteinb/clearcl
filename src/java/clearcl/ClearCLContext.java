package clearcl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import clearcl.enums.DataType;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageChannelType;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;

public class ClearCLContext extends ClearCLBase
{

	private final ClearCLDevice mDevice;

	private final ClearCLQueue mDefaultQueue;

	public ClearCLContext(ClearCLDevice pClearCLDevice,
												ClearCLPeerPointer pContextPointer)
	{
		super(pClearCLDevice.getBackend(), pContextPointer);
		mDevice = pClearCLDevice;

		mDefaultQueue = createQueue();
	}

	public ClearCLQueue getDefaultQueue()
	{
		return mDefaultQueue;
	}

	public ClearCLQueue createQueue()
	{
		ClearCLPeerPointer lQueuePointer = getBackend().createQueue(mDevice.getPeerPointer(),
																																getPeerPointer(),
																																true);
		ClearCLQueue lClearCLQueue = new ClearCLQueue(this, lQueuePointer);
		return lClearCLQueue;
	}

	public ClearCLBuffer createBuffer(HostAccessType pHostAccessType,
																		KernelAccessType pKernelAccessType,
																		DataType pDataType,
																		long pBufferLengthInElements)
	{

		long lBufferSizeInBytes = pBufferLengthInElements * pDataType.getSizeInBytes();

		ClearCLPeerPointer lBufferPointer = getBackend().createBuffer(getPeerPointer(),
																																	pHostAccessType,
																																	pKernelAccessType,
																																	lBufferSizeInBytes);

		ClearCLBuffer lClearCLBuffer = new ClearCLBuffer(	this,
																											lBufferPointer,
																											pHostAccessType,
																											pKernelAccessType,
																											pDataType,
																											pBufferLengthInElements);
		return lClearCLBuffer;
	}

	public ClearCLProgram createProgram(String... pSourceCode)
	{
		ClearCLPeerPointer lProgramPointer = getBackend().createProgram(getPeerPointer(),
																																		pSourceCode);

		ClearCLProgram lClearCLProgram = new ClearCLProgram(mDevice,
																												this,
																												lProgramPointer,
																												pSourceCode);
		return lClearCLProgram;
	}

	public ClearCLProgram createProgram(Class<?> pClassForRessource,
																			String... pRessourceNames) throws IOException
	{
		String[] lSourceCodeStringsArray = new String[pRessourceNames.length];

		int i = 0;
		for (String lRessourceName : pRessourceNames)
		{

			InputStream lResourceAsStream = pClassForRessource.getResourceAsStream(lRessourceName);
			String lSourceCode = IOUtils.toString(lResourceAsStream,
																						"UTF-8");

			lSourceCodeStringsArray[i++] = lSourceCode;
		}

		return createProgram(lSourceCodeStringsArray);
	}

	public ClearCLImage createImage(HostAccessType pHostAccessType,
																	KernelAccessType pKernelAccessType,
																	ImageType pImageType,
																	ImageChannelOrder pImageChannelOrder,
																	ImageChannelType pImageChannelType,
																	long pWidth,
																	long pHeight,
																	long pDepth)
	{
		ClearCLPeerPointer lImage = getBackend().createImage(	getPeerPointer(),
																													pHostAccessType,
																													pKernelAccessType,
																													pImageType,
																													pImageChannelOrder,
																													pImageChannelType,
																													pWidth,
																													pHeight,
																													pDepth);

		ClearCLImage lClearCLImage = new ClearCLImage(this,
																									lImage,
																									pHostAccessType,
																									pKernelAccessType,
																									pImageType,
																									pImageChannelOrder,
																									pImageChannelType,
																									pWidth,
																									pHeight,
																									pDepth);

		return lClearCLImage;
	}

	@Override
	public String toString()
	{
		return String.format("ClearCLContext [mDevice=%s]", mDevice);
	}

	@Override
	public void close() throws Exception
	{
		getBackend().releaseContext(getPeerPointer());
	}

}
