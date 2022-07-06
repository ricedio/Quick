package com.quick.search;

import com.quick.search.model.App;
import com.quick.search.model.Contact;
import com.quick.search.model.Plugin;
import com.quick.search.model.Sms;
import com.quick.search.model.Web;
import com.quick.search.model.info.Searchable;

public class SearchFactory {
    public static Searchable searchFactoryMethod(Locate.Range range) {
        Searchable searchable = null;
        switch (range) {
            case APP:
                searchable = App.getInstance();
                break;
            case CONTACT:
                searchable = Contact.getInstance();
                break;
            case SMS:
                searchable = Sms.getInstance();
                break;
            case PLUGIN:
                searchable = Plugin.getInstance();
                break;
            case WEB:
                searchable = Web.getInstance();
                break;
        }
        return searchable;
    }
}
