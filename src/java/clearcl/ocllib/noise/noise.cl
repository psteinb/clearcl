

#if 1
// Wang Hash based RNG
//  Has at least 20 separate cycles, shortest cycle is < 7500 long.  
//  But it yields random looking 2D noise when fed OpenCL work item IDs, 
//  and that short cycle should only be hit for one work item in about 500K.
inline unsigned int rnguint1( unsigned int x )
{
  unsigned int value = x;

  value = (value ^ 61) ^ (value>>16);
  value *= 9;
  value ^= value << 4;
  value *= 0x27d4eb2d;
  value ^= value >> 15;

  return value;
}

#else

// Unix OS RNG - fast, single cycle of all 2^32 numbers, 
//    but not very random looking when used with OpenCL work item IDs.
inline unsigned int rnguint1( unsigned int x )
{
  unsigned int value = x;

  value = 1103515245 * value + 12345;

  return value;
}

#endif




inline unsigned int rnguint2( unsigned int x,  unsigned int y )
{
  unsigned int value = rnguint1(x);

  value = rnguint1( y ^ value );

  return value;
}


inline unsigned int rnguint3( unsigned int x,  unsigned int y,  unsigned int z )
{
  unsigned int value = rnguint1(x);

  value = rnguint1( y ^ value );

  value = rnguint1( z ^ value );

  return value;
}

inline float rngfloat1(unsigned int x)
{
  const unsigned int value = rnguint1(x);
  const float fvalue = ((float)(value & 0xFFFFFFFF)) / (float)(0xFFFFFFFF);
  return fvalue;
}

inline float rngfloat2(unsigned int x,  unsigned int y)
{
  const unsigned int value = rnguint2(x,y);
  const float fvalue = ((float)(value & 0xFFFFFFFF)) / (float)(0xFFFFFFFF);
  return fvalue;
}

inline float rngfloat3(unsigned int x,  unsigned int y,  unsigned int z)
{
  const unsigned int value = rnguint3(x,y,z);
  const float fvalue = ((float)(value & 0xFFFFFFFF)) / (float)(0xFFFFFFFF);
  return fvalue;
}


