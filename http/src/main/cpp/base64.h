/*base_64.h文件*/
#ifndef BASE_64_H
#define BASE_64_H
/**
 * Base64 编码/解码
 * @author liruixing
 */
class Base64{
private:
    std::string _base64_table;
    static const char base64_pad = '=';public:
    Base64()
    {
        _base64_table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"; /*这是Base64编码使用的标准字典*/
    }
    /**
     * 这里必须是unsigned类型，否则编码中文的时候出错
     */
    std::string Encode(const unsigned char * str,int bytes);
    std::string Decode(const char *str,int bytes);
    int toStrlen(const char *str);
    void Debug(bool open = true);
};
#endif