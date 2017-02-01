

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



inline unsigned int rnguint2(unsigned int x,  unsigned int y )
{
  unsigned int value = rnguint1(x);
  value = rnguint1( y ^ value );
  return value;
}


inline unsigned int rnguint3(unsigned int x,  unsigned int y,  unsigned int z )
{
  unsigned int value = rnguint1(x);
  value = rnguint1( y ^ value );
  value = rnguint1( z ^ value );
  return value;
}


inline int rngsign1(unsigned int x )
{
  return 2*(rnguint1(x) & 1)-1;
}

inline int rngsign2(unsigned int x, unsigned int y )
{
  return 2*(rnguint2(x,y) & 1)-1;;
}

inline int rngsign3(unsigned int x, unsigned int y, unsigned int z )
{
  return 2*(rnguint3(x,y,z) & 1)-1;
}

inline float rngfloat(unsigned int x)
{
  return ((float)(x & 0xFFFFFFFE)) / (float)(0xFFFFFFFF);
}

inline float rngfloat1(unsigned int x)
{
  return rngfloat(rnguint1(x));
}

inline float rngfloat2(unsigned int x,  unsigned int y)
{
  return rngfloat(rnguint2(x,y));
}

inline float rngfloat3(unsigned int x,  unsigned int y,  unsigned int z)
{
  return rngfloat(rnguint3(x,y,z));
}


inline float normal(float f,  float g)
{
  return native_sqrt(-2.0f * native_log(f)) * native_sin(2.0f*M_PI * g);
}

inline float normal1(unsigned int x)
{
  const float f = FLT_MIN+rngfloat1(x);
  const float g = rngfloat1(~x);
  return normal(f,g);
}

inline float normal2(unsigned int x,  unsigned int y)
{
  const float f = FLT_MIN+rngfloat2(x,y);
  const float g = rngfloat2(~y,~x);
  return normal(f,g);
}

inline float normal3(unsigned int x,  unsigned int y, unsigned int z)
{
  const float f = FLT_MIN+rngfloat3(x,y,z);
  const float g = rngfloat3(~z,~x,~y);
  return normal(f,g);
}

inline float fast_normal(float f)
{
  return native_log(FLT_MIN+ f * native_recip(1-f));
}

inline float fast_normal1(unsigned int x)
{
  const float f = rngfloat1(x);
  return fast_normal(f);
}

inline float fast_normal2(unsigned int x,  unsigned int y)
{
  const float f = rngfloat2(x, y);
  return fast_normal(f);
}

inline float fast_normal3(unsigned int x,  unsigned int y, unsigned int z)
{
  const float f = rngfloat3(x, y, z);
  return fast_normal(f);
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


