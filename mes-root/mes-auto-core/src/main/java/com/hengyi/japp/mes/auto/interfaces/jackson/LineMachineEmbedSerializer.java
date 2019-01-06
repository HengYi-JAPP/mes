package com.hengyi.japp.mes.auto.interfaces.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hengyi.japp.mes.auto.domain.LineMachine;

import java.io.IOException;

/**
 * @author jzb 2018-06-28
 */
public class LineMachineEmbedSerializer extends JsonSerializer<LineMachine> {
    @Override
    public void serialize(LineMachine value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            return;
        }

        gen.writeStartObject();

        gen.writeStringField("id", value.getId());
        gen.writeNumberField("item", value.getItem());
        gen.writeNumberField("spindleNum", value.getSpindleNum());
        gen.writeObjectField("line", value.getLine());

        gen.writeEndObject();
    }
}
