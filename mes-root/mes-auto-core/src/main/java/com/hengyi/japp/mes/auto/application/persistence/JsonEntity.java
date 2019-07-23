package com.hengyi.japp.mes.auto.application.persistence;

import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jzb 2018-06-21
 */
@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class JsonEntity implements Serializable {
    @ToString.Include
    @EqualsAndHashCode.Include
    @Id
    @NotBlank
    protected String id;
    @ToString.Include
    protected boolean deleted;

    // todo 删除方法
    public boolean _isNew() {
        throw new IllegalAccessError("_isNew 应该调用代理");
    }

    /**
     * @return 保存实体，并返回 id
     */
    public Single<String> _save() {
        throw new IllegalAccessError("_save 应该调用代理");
    }

    /**
     * 删除实体，数据库删除
     * 推荐，打删除标记，然后保存
     *
     * @return
     */
    public Completable _delete() {
        throw new IllegalAccessError("_delete 应该调用代理");
    }

}
