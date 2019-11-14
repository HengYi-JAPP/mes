package com.hengyi.japp.mes.auto.interfaces.search;

import com.google.inject.ImplementedBy;
import com.hengyi.japp.mes.auto.interfaces.search.internal.SearchServiceImpl;

/**
 * @author jzb 2019-11-14
 */
@ImplementedBy(SearchServiceImpl.class)
public interface SearchService {
}
