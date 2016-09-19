package clearcl;

public class ClearCLQueue extends ClearCLBase
{

	public ClearCLQueue(ClearCLContext pClearCLContext,
											ClearCLPeerPointer pQueuePointer)
	{
		super(pClearCLContext.getBackend(), pQueuePointer);
	}
	
	
	public void waitToFinish()
	{
		getBackend().waitQueueToFinish(getPeerPointer());
	}
	

	@Override
	public void close() throws Exception
	{
		getBackend().releaseQueue(getPeerPointer());
	}

}
