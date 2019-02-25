package com.hengyi.japp.mes.auto.application.command;

import com.hengyi.japp.mes.auto.dto.EntityDTO;
import io.vertx.core.json.JsonObject;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;

import static com.github.ixtf.japp.vertx.Jvertx.readCommand;


/**
 * @author jzb 2018-06-21
 */
@Data
public class ProductPlanNotifyExeCommand implements Serializable {
    @NotNull
    private EntityDTO lineMachine;

    public static ProductPlanNotifyExeCommand from(JsonObject jsonObject) throws Exception {
        return readCommand(ProductPlanNotifyExeCommand.class, jsonObject.encode());
    }

    @Data
    public static class Batch {
        @NotNull
        @Size(min = 1)
        private Collection<EntityDTO> lineMachines;
    }
}
