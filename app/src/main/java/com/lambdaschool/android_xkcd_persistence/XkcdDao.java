package com.lambdaschool.android_xkcd_persistence;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class XkcdDao {
    private static final String URL_BASE = "https://xkcd.com/";
    private static final String URL_ENDING = "info.0.json";
    private static final String URL_RECENT = "https://xkcd.com/info.0.json";
    private static final String URL_SPECIFIC = "https://xkcd.com/%d/info.0.json";
    public static int maxComicNumber;

    private static XkcdComic getComic(String url) {
        XkcdComic xkcdComic = null;
        try {
            JSONObject json = new JSONObject(NetworkAdapter.httpRequest(url));
            xkcdComic = new XkcdComic(json);
            Bitmap bitmap = NetworkAdapter.httpImageRequest(xkcdComic.getImg());
            xkcdComic.setBitmap(bitmap);

            XkcdDbInfo xkcdDbInfo = XkcdSqlDao.readComic(Integer.parseInt(xkcdComic.getNum()));

            if (xkcdDbInfo == null) {
                xkcdDbInfo = new XkcdDbInfo();
                xkcdComic.setXkcdDbInfo(xkcdDbInfo);
                XkcdSqlDao.createComic(xkcdComic);

                xkcdComic.getXkcdDbInfo().setTimestamp(System.currentTimeMillis());
                XkcdSqlDao.updateComic(xkcdComic);
            }

            xkcdComic.setXkcdDbInfo(xkcdDbInfo);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return xkcdComic;
    }

    public static XkcdComic getRecentComic() {
        XkcdComic xkcdComic = getComic(URL_RECENT);
        maxComicNumber = Integer.valueOf(xkcdComic.getNum());

        return xkcdComic;
    }

    public static XkcdComic getNextComic(XkcdComic xkcdComic) {
        int num = Integer.valueOf(xkcdComic.getNum()) + 1;

        return getComic(String.format(Locale.US, URL_SPECIFIC, num));
    }

    public static XkcdComic getPreviousComic(XkcdComic xkcdComic) {
        int num = Integer.valueOf(xkcdComic.getNum()) - 1;

        return getComic(String.format(Locale.US, URL_SPECIFIC, num));
    }

    public static XkcdComic getRandomComic() {
        int randomNum = (int) ((Math.random() * maxComicNumber) + 1);

        return getComic(String.format(Locale.US, URL_SPECIFIC, randomNum));
    }

    public static XkcdComic getSpecificComic(String comicNum) {
        return getComic(URL_BASE + comicNum + "/" + URL_ENDING);
    }

    public static void setFavorite(XkcdComic xkcdComic) {
        XkcdSqlDao.updateComic(xkcdComic);
    }

    public static ArrayList<XkcdDbInfo> getFavorites() {
        return XkcdSqlDao.readFavoriteComics();
    }
}
