/*base_64.cpp文件*/
#include <iostream>
#include <string>
#include <cstring>
#include "base64.h"

std::string Base64::Encode(const unsigned char *str, int bytes) {
    int num = 0, bin = 0, i;
    std::string _encode_result;
    const unsigned char *current;
    current = str;
    while (bytes > 2) {
        _encode_result += _base64_table[current[0] >> 2];
        _encode_result += _base64_table[((current[0] & 0x03) << 4) + (current[1] >> 4)];
        _encode_result += _base64_table[((current[1] & 0x0f) << 2) + (current[2] >> 6)];
        _encode_result += _base64_table[current[2] & 0x3f];

        current += 3;
        bytes -= 3;
    }
    if (bytes > 0) {
        _encode_result += _base64_table[current[0] >> 2];
        if (bytes % 3 == 1) {
            _encode_result += _base64_table[(current[0] & 0x03) << 4];
            _encode_result += "==";
        } else if (bytes % 3 == 2) {
            _encode_result += _base64_table[((current[0] & 0x03) << 4) + (current[1] >> 4)];
            _encode_result += _base64_table[(current[1] & 0x0f) << 2];
            _encode_result += "=";
        }
    }
    return _encode_result;
}

std::string Base64::Decode(const char *str, int length) {
    //解码表
    const char DecodeTable[] =
            {
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-1),
                    static_cast<char>(-1), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-1), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-1), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(62), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(63),
                    static_cast<char>(52), static_cast<char>(53), static_cast<char>(54),
                    static_cast<char>(55), static_cast<char>(56), static_cast<char>(57),
                    static_cast<char>(58), static_cast<char>(59), static_cast<char>(60),
                    static_cast<char>(61), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(0), static_cast<char>(1),
                    static_cast<char>(2), static_cast<char>(3), static_cast<char>(4),
                    static_cast<char>(5), static_cast<char>(6), static_cast<char>(7),
                    static_cast<char>(8), static_cast<char>(9), static_cast<char>(10),
                    static_cast<char>(11), static_cast<char>(12), static_cast<char>(13),
                    static_cast<char>(14),
                    static_cast<char>(15), static_cast<char>(16), static_cast<char>(17),
                    static_cast<char>(18), static_cast<char>(19), static_cast<char>(20),
                    static_cast<char>(21), static_cast<char>(22), static_cast<char>(23),
                    static_cast<char>(24),
                    static_cast<char>(25), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(26), static_cast<char>(27),
                    static_cast<char>(28),
                    static_cast<char>(29), static_cast<char>(30), static_cast<char>(31),
                    static_cast<char>(32), static_cast<char>(33), static_cast<char>(34),
                    static_cast<char>(35), static_cast<char>(36),
                    static_cast<char>(37), static_cast<char>(38), static_cast<char>(39),
                    static_cast<char>(40),
                    static_cast<char>(41), static_cast<char>(42), static_cast<char>(43),
                    static_cast<char>(44), static_cast<char>(45), static_cast<char>(46),
                    static_cast<char>(47), static_cast<char>(48), static_cast<char>(49),
                    static_cast<char>(50), static_cast<char>(51),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>( -2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>( -2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2), static_cast<char>(-2),
                    static_cast<char>(-2), static_cast<char>(-2)
            };
    int bin = 0, i = 0, pos = 0;
    std::string _decode_result;
    const char *current = str;
    char ch;
    while ((ch = *current++) != '\0' && length-- > 0) {
        if (ch == base64_pad) { // 当前一个字符是“=”号
            /*
            先说明一个概念：在解码时，4个字符为一组进行一轮字符匹配。
            两个条件：
                1、如果某一轮匹配的第二个是“=”且第三个字符不是“=”，说明这个带解析字符串不合法，直接返回空
                2、如果当前“=”不是第二个字符，且后面的字符只包含空白符，则说明这个这个条件合法，可以继续。
            */
            if (*current != '=' && (i % 4) == 1) {
                return NULL;
            }
            continue;
        }
        ch = DecodeTable[ch];
        //这个很重要，用来过滤所有不合法的字符
        if (ch < 0) { /* a space or some other separator character, we simply skip over */
            continue;
        }
        switch (i % 4) {
            case 0:
                bin = ch << 2;
                break;
            case 1:
                bin |= ch >> 4;
                _decode_result += bin;
                bin = (ch & 0x0f) << 4;
                break;
            case 2:
                bin |= ch >> 2;
                _decode_result += bin;
                bin = (ch & 0x03) << 6;
                break;
            case 3:
                bin |= ch;
                _decode_result += bin;
                break;
        }
        i++;
    }
    return _decode_result;
}

int Base64::toStrlen(const char *str) {
    return strlen(str);
}