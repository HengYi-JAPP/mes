package com.hengyi.japp.mes.auto.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.hengyi.japp.mes.auto.application.persistence.annotations.JsonEntityProperty;
import com.hengyi.japp.mes.auto.domain.data.FormFieldConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.github.ixtf.japp.core.Constant.MAPPER;


/**
 * @author jzb 2018-07-27
 */
@Data
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class FormConfig extends LoggableMongoEntity {
    @ToString.Include
    private String name;
    @JsonIgnore
    @JsonEntityProperty("config")
    private String configString;

    @JsonGetter("formFieldConfigs")
    public Collection<FormFieldConfig> formFieldConfigs() throws IOException {
        final CollectionLikeType type = MAPPER.getTypeFactory().constructCollectionLikeType(ArrayList.class, FormFieldConfig.class);
        return MAPPER.readValue(getConfigString(), type);
    }

    public void formFieldsConfig(Collection<FormFieldConfig> formFieldConfigs) throws JsonProcessingException {
        final Collection<FormFieldConfig> value = formFieldConfigs
                .stream()
                .map(it -> {
                    if (StringUtils.isBlank(it.getId())) {
                        it.setId(new ObjectId().toHexString());
                    }
                    return it;
                })
                .collect(Collectors.toList());
        setConfigString(MAPPER.writeValueAsString(value));
    }

    @Data
    public static class FormFieldValue {
        @NotBlank
        private String id;
        private String valueString;
        //多选字段的值
        private Collection<String> valuesString;
    }

}
