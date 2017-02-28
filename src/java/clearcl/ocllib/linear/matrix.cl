

inline float16 matrix_load(int offset, __constant float* pointer) 
{
    float16 matrix = vload16(offset, pointer);
    return matrix;
}


inline float4 matrix_mult(float16 matrix, float4 vector) 
{
  const float4 result;
  result.x = dot(vector, ((float4)(matrix[0],matrix[1],matrix[2],matrix[3])));
  result.y = dot(vector, ((float4)(matrix[4],matrix[5],matrix[6],matrix[7])));
  result.z = dot(vector, ((float4)(matrix[8],matrix[9],matrix[10],matrix[11])));
  result.w = dot(vector, ((float4)(matrix[12],matrix[13],matrix[14],matrix[15])));    
  return result;             
}

inline float16 matrix_transpose(float16 matrix) 
{
  return matrix.s048C159D26AE37BF;
}

