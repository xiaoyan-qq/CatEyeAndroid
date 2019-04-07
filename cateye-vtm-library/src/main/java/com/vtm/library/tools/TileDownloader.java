package com.vtm.library.tools;

import org.oscim.core.Tile;
import org.oscim.tiling.ITileCache;
import org.oscim.tiling.ITileDataSource;
import org.oscim.tiling.source.HttpEngine;
import org.oscim.tiling.source.LwHttp;
import org.oscim.tiling.source.UrlTileSource;
import org.oscim.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import static org.oscim.tiling.QueryResult.FAILED;
import static org.oscim.tiling.QueryResult.SUCCESS;

public class TileDownloader {
    private UrlTileSource mUrlTileSource;
    private ITileCache mTileCache;
    private HttpEngine mHttpEngine;

    public TileDownloader(UrlTileSource mUrlTileSource, ITileCache mTileCache) {
        this.mUrlTileSource = mUrlTileSource;
        this.mTileCache = mTileCache;
        this.mHttpEngine = new LwHttp.LwHttpFactory().create(mUrlTileSource);
    }

    public void download(Tile mTile) {
        ITileCache.TileReader c = mTileCache.getTile(mTile);
        if (c != null) {
            return;
        }
        ITileCache.TileWriter cacheWriter = null;
        try {
            mHttpEngine.sendRequest(mTile);
            cacheWriter = mTileCache.writeTile(mTile);
            mHttpEngine.setCache(cacheWriter.getOutputStream());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (cacheWriter != null){
                cacheWriter.complete(true);
            }
            mHttpEngine.requestCompleted(true);
        }
    }
}
