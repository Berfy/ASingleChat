package cn.berfy.sdk.mvpbase.pictureselector.entities;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * created by arvin on 16/8/28 22:34
 * email：1035407623@qq.com
 */
public class ImagesModel {
    public static Observable<List<ImageFolderEntity>> getImageFolders(Context context){
        return Observable.just(context).map(context1 -> {
            //图集列表
            List<ImageFolderEntity> imageFolders = new ArrayList<>();
            //所有图片列表
            List<ImageEntity> allImages = new ArrayList<>();
            Cursor cursor = getImageCursor(context1);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //获取当前游标所指的图片信息
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                    long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    ImageEntity entity = new ImageEntity(path, time, size);
                    if (entity.getSize() / 1024 >= 10) {
                        allImages.add(entity);
                    }

                    //将当前图片加入到相应的文件图集中
                    boolean needAdd = true;
                    File parentFile = new File(path).getParentFile();
                    for (int i = 0; i < imageFolders.size(); i++) {
                        if (imageFolders.get(i).getFolderName().equals(parentFile.getName())) {
                            imageFolders.get(i).setCount(imageFolders.get(i).getCount() + 1);
                            imageFolders.get(i).getImages().add(entity);
                            needAdd = false;
                            break;
                        }
                    }
                    if (needAdd) {
                        ImageFolderEntity imageFolder = new ImageFolderEntity();
                        imageFolder.setCount(1);
                        imageFolder.setFirstImagePath(path);
                        imageFolder.setFolderName(parentFile.getName());
                        List<ImageEntity> images = new ArrayList<>();
                        images.add(entity);
                        imageFolder.setImages(images);
                        imageFolders.add(imageFolder);
                    }
                }
                cursor.close();

                //创建一个所有图片的图集
                if (allImages.size() != 0) {
                    ImageFolderEntity imageFolder = new ImageFolderEntity();
                    imageFolder.setCount(allImages.size());
                    imageFolder.setFirstImagePath(allImages.get(0).getPath());
                    imageFolder.setFolderName("所有图片");
                    imageFolder.setImages(allImages);
                    imageFolders.add(0, imageFolder);
                }
            }
            return imageFolders;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    private static Cursor getImageCursor(Context context) {
        String[] supportMineTypes = getSupportMineTypes();
        return context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                getSelection(supportMineTypes), supportMineTypes, MediaStore.Images.Media.DATE_MODIFIED + " desc");
    }

    private static String getSelection(String[] supportMineTypes) {
        StringBuilder selection = new StringBuilder();
        for (int i = 0; i < supportMineTypes.length; i++) {
            selection.append(MediaStore.Images.Media.MIME_TYPE + "=?");
            if (i != supportMineTypes.length - 1) {
                selection.append(" or ");
            }
        }
        return selection.toString();
    }

    private static String[] getSupportMineTypes() {
        return new String[]{"image/jpeg", "image/png"};
    }
}
