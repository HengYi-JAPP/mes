package com.hengyi.japp.mes.auto.repository;

import com.hengyi.japp.mes.auto.domain.Permission;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * @author jzb 2018-06-24
 */
public interface PermissionRepository {

    Single<Permission> create();

    Single<Permission> save(Permission permission);

    Single<Permission> find(String id);

    Flowable<Permission> list();

    Flowable<Permission> autoComplete(String q);
}