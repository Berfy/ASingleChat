package cn.berfy.service.download;

/**
 * 下载文件详情
 *
 * @author NorthStar
 * @date 2018/7/30 10:35
 */
public class FileInfo {
    private String fileName;//文件名
    private String url;//下载地址
    private String filePath;//下载目录

    public FileInfo(String url) {
        this.url = url;
    }

    public FileInfo(String filePath, String url) {
        this.filePath = filePath;
        this.url = url;
    }

    public FileInfo(String url, String filePath, String fileName) {
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
