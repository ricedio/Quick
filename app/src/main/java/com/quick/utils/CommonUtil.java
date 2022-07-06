package com.quick.utils;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;

/**
 * This is a common util class for those methods which has not specified type.
 */
public class CommonUtil {

    /**
     * When doing a web search, there are two situations.
     *
     * If user type in a url or something similar to a url, we need to
     * open the url directly for user. Otherwise, we just search the
     * content user type in with default search engine.
     *
     * @param str content user want to search or url user want to visit
     * @return intent
     */
    public static Intent getWebSearchIntent(String str) {
        Intent intent;
        // if search content contain ".", we think it's a url
        if(str.contains(".")) {
            if (!str.startsWith("http://") && !str.startsWith("https://"))
                str = "http://" + str;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
        } else {
            intent = new Intent( Intent.ACTION_WEB_SEARCH );
            intent.putExtra( SearchManager.QUERY , str );
        }
        return intent;
    }

}
