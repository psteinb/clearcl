

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


inline float normal1(unsigned int x)
{
  const float u1 = FLT_MIN+rngfloat1(x);
  const float u2 = rngfloat1(~x);
 
  float normal = native_sqrt(-2.0f * native_log(u1)) * native_sin(2.0f*M_PI * u2);
  return normal;
}

inline float normal2(unsigned int x,  unsigned int y)
{
  const float u1 = FLT_MIN+rngfloat2(x,y);
  const float u2 = rngfloat2(~y,~x);
   
  float normal = native_sqrt(-2.0f * native_log(u1)) * native_sin(2.0f*M_PI * u2);
  return normal;
}

inline float normal3(unsigned int x,  unsigned int y, unsigned int z)
{
  const float u1 = FLT_MIN+rngfloat3(x,y,z);
  const float u2 = rngfloat3(~z,~x,~y);

  float normal = native_sqrt(-2.0f * native_log(u1)) * native_sin(2.0f*M_PI * u2);
  return normal;
}


inline float anscomb_transform(const float x)
{
  return 2.0f*native_sqrt(x+0.375f);
}

inline float inverse_anscomb_transform(const float x)
{
  return 0.25f*(x*x)-0.375f;
}

inline float poisson1(float m, unsigned int x)
{
  const float mean = 2.0f*native_sqrt(m+3.0f/8)-native_recip(4*native_sqrt(m));
  
  const float normalvalue = mean + normal1(x);
  
  const float poissonvalue = inverse_anscomb_transform(normalvalue);
  
  return poissonvalue;
}

inline float poisson2(float m, unsigned int x, unsigned int y)
{
  const float mean = 2.0f*native_sqrt(m+3.0f/8)-native_recip(4*native_sqrt(m));
  
  const float normalvalue = mean + normal2(x,y);
  
  const float poissonvalue = inverse_anscomb_transform(normalvalue);
  
  return poissonvalue;
}

inline float poisson3(float m, unsigned int x, unsigned int y, unsigned int z)
{
  const float mean = 2.0f*native_sqrt(m+3.0f/8)-native_recip(4*native_sqrt(m));
  
  const float normalvalue = mean + normal3(x,y,z);
  
  const float poissonvalue = inverse_anscomb_transform(normalvalue);
  
  return poissonvalue;
}


