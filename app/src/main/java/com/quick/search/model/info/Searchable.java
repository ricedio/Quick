package com.quick.search.model.info;

import java.util.List;

public interface Searchable {
    List<SearchResultInfo> search(String str);
}
