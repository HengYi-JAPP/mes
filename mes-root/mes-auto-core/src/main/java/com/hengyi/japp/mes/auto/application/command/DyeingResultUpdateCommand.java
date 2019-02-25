package com.hengyi.japp.mes.auto.application.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.hengyi.japp.mes.auto.dto.EntityDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author jzb 2018-11-17
 */
@Data
public class DyeingResultUpdateCommand implements Serializable {
    @NotNull
    @Size(min = 1)
    private Collection<Item> items;

    @Data
    public static final class Item {
        @NotNull
        private EntityDTO silk;
        private boolean hasException;
        private EntityDTO grade;
        private Collection<EntityDTO> silkExceptions;
        private Collection<EntityDTO> silkNotes;
        private JsonNode formConfig;
        private JsonNode formConfigValueData;
    }

    @Data
    public static final class Batch implements Serializable {
        @NotNull
        @Size(min = 1)
        private Collection<BatchItem> commands;
    }

    @Data
    public static final class BatchItem {
        @NotNull
        private EntityDTO dyeingPrepare;
        @NotNull
        @Size(min = 1)
        private Collection<Item> items;
    }
}
