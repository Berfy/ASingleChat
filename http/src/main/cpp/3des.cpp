#include "memory.h"
#include "3des.h"
#include "stdio.h"
#include <stdlib.h>

/*********************************************************/
void F_func(bool In[32], const bool Ki[48]); 
void S_func(bool Out[32], const bool In[48]); 
void Transform(bool *Out, bool *In, const char *Table, int len); // 
void Xor(bool *InA, const bool *InB, int len); // 
void RotateL(bool *In, int len, int loop); //
void ByteToBit(bool *Out, const char *In, int bits); //
void BitToByte(char *Out, const bool *In, int bits); // 
bool SubKey[16][48]; // 16ȦԿ
/*********************************************************/

/*********************************************************/
int Encrypt(char *Msg, char *Key, char *Cipher, int length) //ϢȲ8ıĩλ0ճ8ı
{
	if (length <= 0) {
		return -1;
	}
	int OutLength = (length % 8 == 0) ? length : ((1 + (length / 8)) * 8);
	char keyarray1[8];
	char keyarray2[8];
	char keyarray3[8];
	memcpy(&keyarray1, &Key[0], 8);
	memcpy(&keyarray2, &Key[8], 8);
	memcpy(&keyarray3, &Key[16], 8);
	if (length % 8 == 0) {
		int dst = length / 8;
		char out[8];
		char in[8];
		int j;
		for (j = 0; j < dst; j++) {
			memcpy(&in[0], &Msg[8 * j], 8);
			encrypt(in, keyarray1, out); ///////////////////
			decrypt(out, keyarray2, in); //////////////////
			encrypt(in, keyarray3, out); //////////////////
			memcpy(&Cipher[8 * j], &out, 8);
		}
		return OutLength;
	} else {
		int ext = length / 8;
		int dst = length % 8;
		char * temp_in;
		char * temp_add;
		temp_in = (char *) malloc((ext + 1) * 8);
		temp_add = (char *) malloc(8 - dst);
		memcpy(&temp_in[0], &Msg[0], length);
		memset(temp_add, 0, 8 - dst);
		memcpy(&temp_in[length], &temp_add[0], 8 - dst);

		int round = ext + 1;
		char out[8];
		char in[8];
		int j;
		for (j = 0; j < round; j++) {
			memcpy(&in[0], &temp_in[8 * j], 8);
			encrypt(in, keyarray1, out);
			decrypt(out, keyarray2, in);
			encrypt(in, keyarray3, out);
			memcpy(&Cipher[8 * j], &out, 8);
		}
		return OutLength;
	}
}
void encrypt(char In[8], const char Key[8], char Out[8]) {
	Des_SetKey(Key);
	static bool M[64], Tmp[32], *Li = &M[0], *Ri = &M[32];
	ByteToBit(M, In, 64);
	Transform(M, M, IP_Table, 64);
	int i;
	for (i = 0; i < 16; i++) {
		memcpy(Tmp, Ri, 32);
		F_func(Ri, SubKey[i]);
		Xor(Ri, Li, 32);
		memcpy(Li, Tmp, 32);
	}

	Transform(M, M, IPR_Table, 64);
	BitToByte(Out, M, 64);
}
/*********************************************************/
int Decrypt(char *Msg, char *Key, char *Cipher, int length) //ϢȲ8ıĩλ0ճ8ı
{
	if (length <= 0) {
		return -1;
	}
	int OutLength = (length % 8 == 0) ? length : ((1 + (length / 8)) * 8);
	char keyarray1[8];
	char keyarray2[8];
	char keyarray3[8];
	memcpy(&keyarray1, &Key[0], 8);
	memcpy(&keyarray2, &Key[8], 8);
	memcpy(&keyarray3, &Key[16], 8);
	if (length % 8 == 0) {
		int dst = length / 8;
		char out[8];
		char in[8];
		int j;
		for (j = 0; j < dst; j++) {
			memcpy(&in[0], &Msg[8 * j], 8);
			decrypt(in, keyarray3, out);
			encrypt(out, keyarray2, in);
			decrypt(in, keyarray1, out);
			memcpy(&Cipher[8 * j], &out, 8);
		}
		return OutLength;
	} else {
		int ext = length / 8;
		int dst = length % 8;
		char * temp_in;
		char * temp_add;
		temp_in = (char *) malloc((ext + 1) * 8);
		temp_add = (char *) malloc(8 - dst);
		memcpy(&temp_in[0], &Msg[0], length);
		memset(temp_add, 0, 8 - dst);
		memcpy(&temp_in[length], &temp_add[0], 8 - dst);

		int round = ext + 1;
		char out[8];
		char in[8];
		int j;
		for (j = 0; j < round; j++) {
			memcpy(&in[0], &temp_in[8 * j], 8);
			decrypt(in, keyarray3, out);
			encrypt(out, keyarray2, in);
			decrypt(in, keyarray1, out);
			memcpy(&Cipher[8 * j], &out, 8);
		}
		return OutLength;
	}
}
void decrypt(char In[8], const char Key[8], char Out[8]) {
	Des_SetKey(Key);
	static bool M[64], Tmp[32], *Li = &M[0], *Ri = &M[32];
	ByteToBit(M, In, 64);
	Transform(M, M, IP_Table, 64);
	int i;
	for (i = 15; i >= 0; i--) {
		memcpy(Tmp, Li, 32);
		F_func(Li, SubKey[i]);
		Xor(Li, Ri, 32);
		memcpy(Ri, Tmp, 32);
	}
	Transform(M, M, IPR_Table, 64);
	BitToByte(Out, M, 64);
}
/********************Կ*****************************/
void Des_SetKey(const char Key[8]) {
	static bool K[64], *KL = &K[0], *KR = &K[28];
	ByteToBit(K, Key, 64);
	Transform(K, K, PC1_Table, 56);
	int i;
	for (i = 0; i < 16; i++) {
		RotateL(KL, 28, LOOP_Table[i]);
		RotateL(KR, 28, LOOP_Table[i]);
		Transform(SubKey[i], K, PC2_Table, 48);
	}
}
/*********************************************************/
void F_func(bool In[32], const bool Ki[48]) {
	static bool MR[48];
	Transform(MR, In, E_Table, 48);
	Xor(MR, Ki, 48);
	S_func(In, MR);
	Transform(In, In, P_Table, 32);
}
void S_func(bool Out[32], const bool In[48]) {
	char i, j, k;
	for (i = 0; i < 8; i++, In += 6, Out += 4) {
		j = (In[0] << 1) + In[5];
		k = (In[1] << 3) + (In[2] << 2) + (In[3] << 1) + In[4];
		ByteToBit(Out, &S_Box[i][j][k], 4);
	}
}
void Transform(bool *Out, bool *In, const char *Table, int len) {
	static bool Tmp[256];
	int i;
	for (i = 0; i < len; i++)
		Tmp[i] = In[Table[i] - 1];
	memcpy(Out, Tmp, len);
}
void Xor(bool *InA, const bool *InB, int len) {
	int i;
	for (i = 0; i < len; i++)
		InA[i] ^= InB[i];
}
void RotateL(bool *In, int len, int loop) {
	static bool Tmp[256];
	memcpy(Tmp, In, loop);
	memcpy(In, In + loop, len - loop);
	memcpy(In + len - loop, Tmp, loop);
}
void ByteToBit(bool *Out, const char *In, int bits) {
	int i;
	for (i = 0; i < bits; i++)
		Out[i] = (In[i / 8] >> (i % 8)) & 1;
}
void BitToByte(char *Out, const bool *In, int bits) {
	memset(Out, 0, (bits + 7) / 8);
	int i;
	for (i = 0; i < bits; i++)
		Out[i / 8] |= In[i] << (i % 8);
}
/********************* end *********************************/
