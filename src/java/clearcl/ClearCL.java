package clearcl;

import clearcl.backend.ClearCLBackendInterface;

public class ClearCL extends ClearCLBase
{

	public ClearCL(ClearCLBackendInterface pClearCLBackendInterface)
	{
		super(pClearCLBackendInterface,null);
	}

	public int getNumberOfPlatforms()
	{
		return getBackend().getNumberOfPlatforms();
	}

	public ClearCLPlatform getPlatform(int pPlatformIndex)
	{
		ClearCLPeerPointer lPlatformIdPointer = getBackend().getPlatformIds(pPlatformIndex);
		return new ClearCLPlatform(this, lPlatformIdPointer);
	}

	@Override
	public void close() throws Exception
	{
	}

}
