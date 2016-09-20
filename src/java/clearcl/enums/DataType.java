package clearcl.enums;

public enum DataType
{
	Byte(1),
	SignedByte(1),
	Short(2),
	SignedShort(2),
	Integer(4),
	SignedInteger(4),
	Long(8),
	SignedLong(8),
	HalfFloat(2),
	Float(4),
	Double(8);

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
