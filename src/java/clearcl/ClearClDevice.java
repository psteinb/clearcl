package clearcl;

import clearcl.enums.DeviceType;

public class ClearCLDevice extends ClearCLBase
{

	private ClearCLPlatform mClearCLPlatform;
	private ClearCLPeerPointer mDevicePointer;

	public ClearCLDevice(	ClearCLPlatform pClearCLPlatform,
												ClearCLPeerPointer pDevicePointer)
	{
		super(pClearCLPlatform.getBackend(), pDevicePointer);
		mClearCLPlatform = pClearCLPlatform;
		mDevicePointer = pDevicePointer;
	}

	public String getName()
	{
		return getBackend().getDeviceName(mDevicePointer);
	}

	public DeviceType getType()
	{
		return getBackend().getDeviceType(mDevicePointer);
	}

	public double getVersion()
	{
		String lStringVersion = getBackend().getDeviceVersion(mDevicePointer)
																				.replace("OpenCL C", "")
																				.trim();
		Double lDoubleVersion = Double.parseDouble(lStringVersion);
		return lDoubleVersion;
	}

	public String getInfoString()
	{
		StringBuilder lStringBuilder = new StringBuilder();

		lStringBuilder.append(String.format("Device name: %s, type: %s, OpenCL version: %g  \n",
																				getName(),
																				getType(),
																				getVersion()));

		return lStringBuilder.toString();
	}

	public ClearCLContext createContext()
	{
		ClearCLPeerPointer lContextPointer = getBackend().getContext(	mClearCLPlatform.getPeerPointer(),
																																	mDevicePointer);
		return new ClearCLContext(this, lContextPointer);
	}

	@Override
	public String toString()
	{
		return String.format(	"ClearCLDevice [mClearCLPlatform=%s, getInfoString()=%s]",
													mClearCLPlatform,
													getInfoString());
	}

	@Override
	public void close() throws Exception
	{
		getBackend().releaseDevice(getPeerPointer());
	}

}
