package clearcl;

import clearcl.backend.ClearCLBackendInterface;

public abstract class ClearCLBase implements AutoCloseable
{

	private final ClearCLBackendInterface mClearCLBackendInterface;
	private final ClearCLPeerPointer mPeerPointer;

	public ClearCLBase(ClearCLBackendInterface pClearCLBackendInterface, ClearCLPeerPointer pPointer)
	{
		mClearCLBackendInterface = pClearCLBackendInterface;
		mPeerPointer = pPointer;
	}

	public ClearCLBackendInterface getBackend()
	{
		return mClearCLBackendInterface;
	}

	@Override
	public abstract void close() throws Exception;

	public ClearCLPeerPointer getPeerPointer()
	{
		return mPeerPointer;
	}

}
