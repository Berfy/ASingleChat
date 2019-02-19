package cn.berfy.sdk.mvpbase.util;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;

import java.io.File;

import cn.berfy.sdk.mvpbase.config.CacheConstant;

/**
 * Created by huangjun on 2017/4/1.
 */
public class GGlideModule implements com.bumptech.glide.module.GlideModule {
    private static final String TAG = "GGlideModule";

    private static final int M = 1024 * 1024;
    private static final int MAX_DISK_CACHE_SIZE = 256 * M;
    private static DiskCache diskCache = null;

    /**
     * ************************ Disk Cache ************************
     */
    private static synchronized DiskCache getDiskCache(Context context) {
        if (diskCache == null) {
            diskCache = createDiskCache(context);
        }
        return diskCache;
    }

    private static synchronized DiskCache createDiskCache(Context context) {
        File cacheDir = new File(CacheConstant.DIR_PUBLIC_ROOT + "/cache/image/");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return DiskLruCacheWrapper.get(cacheDir, MAX_DISK_CACHE_SIZE);
    }

    /**
     * ************************ Memory Cache ************************
     */

    static void clearMemoryCache(Context context) {
        Glide.get(context).clearMemory();
    }

    /**
     * ************************ GGlideModule override ************************
     */
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDiskCache(new DiskCache.Factory() {
            @Override
            public DiskCache build() {
                return getDiskCache(context);
            }
        });

        LogF.i(TAG, "GGlideModule apply options");
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
