package clearcl.enums;

public enum DataType
{
	Byte(1), Integer(4), Long(8), Float(4), Double(8);
	
	private final int mSizeInBytes;
	
	DataType(int pSizeInBytes)
	{
		mSizeInBytes = pSizeInBytes;
	}

	public int getSizeInBytes()
	{
		return mSizeInBytes;
	}
}
