package clearcl;

import clearcl.enums.DeviceType;

public class ClearCLPlatform extends ClearCLBase
{

	private ClearCLPeerPointer mPlatformPointer;

	public ClearCLPlatform(	ClearCL pClearCL,
													ClearCLPeerPointer pPlatformIdPointer)
	{
		super(pClearCL.getBackend(), pPlatformIdPointer);
		mPlatformPointer = pPlatformIdPointer;
	}

	public int getNumberOfDevices(DeviceType pDeviceType)
	{
		return getBackend().getNumberOfDevicesForPlatform(mPlatformPointer,
																											pDeviceType);
	}

	public int getNumberOfDevices()
	{
		return getBackend().getNumberOfDevicesForPlatform(mPlatformPointer);
	}

	public ClearCLDevice getCPUDevice(int pCPUDeviceIndex,
																		DeviceType pDeviceType)
	{
		ClearCLPeerPointer lDevicePointer = getBackend().getDeviceId(	mPlatformPointer,
																															pDeviceType,
																															pCPUDeviceIndex);
		return new ClearCLDevice(this, lDevicePointer);
	}

	public ClearCLDevice getDevice(int pDeviceIndex)
	{
		ClearCLPeerPointer lDevicePointer = getBackend().getDeviceId(	mPlatformPointer,
																															pDeviceIndex);
		return new ClearCLDevice(this, lDevicePointer);
	}

	public String getName()
	{
		return getBackend().getPlatformName(mPlatformPointer);
	}

	public String getInfoString()
	{
		StringBuilder lStringBuilder = new StringBuilder();

		lStringBuilder.append(String.format("Platform name: %s \n",
																				getName()));
		lStringBuilder.append(String.format("\tNumber of CPU devices: %d \n",
																				getNumberOfDevices(DeviceType.CPU)));
		lStringBuilder.append(String.format("\tNumber of GPU devices: %d \n",
																				getNumberOfDevices(DeviceType.GPU)));

		return lStringBuilder.toString();
	}

	@Override
	public String toString()
	{
		return String.format(	"ClearCLPlatform [mPlatformIdPointer=%s]\n %s",
													mPlatformPointer,
													getInfoString());
	}

	@Override
	public void close() throws Exception
	{
	}

}
