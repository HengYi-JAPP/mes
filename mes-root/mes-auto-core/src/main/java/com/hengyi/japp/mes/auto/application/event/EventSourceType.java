package com.hengyi.japp.mes.auto.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkCarInfoFetchEvent;
import io.reactivex.Single;

/**
 * @author jzb 2018-08-09
 */
public enum EventSourceType {
    SilkCarRuntimeInitEvent {
        @Override
        public Single<SilkCarRuntimeInitEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent.DTO.from(jsonNode).toEvent();
        }
    },
    SilkCarRuntimeAppendEvent {
        @Override
        public Single<SilkCarRuntimeAppendEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeAppendEvent.DTO.from(jsonNode).toEvent();
        }
    },

    SilkCarRuntimeGradeEvent {
        @Override
        public Single<SilkCarRuntimeGradeEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeGradeEvent.DTO.from(jsonNode).toEvent();
        }
    },

    SilkCarRuntimeWeightEvent {
        @Override
        public Single<SilkCarRuntimeWeightEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeWeightEvent.DTO.from(jsonNode).toEvent();
        }
    },

    SilkRuntimeDetachEvent {
        @Override
        public Single<SilkRuntimeDetachEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkRuntimeDetachEvent.DTO.from(jsonNode).toEvent();
        }
    },

    SilkCarRuntimeGradeSubmitEvent {
        @Override
        public Single<SilkCarRuntimeGradeSubmitEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeGradeSubmitEvent.DTO.from(jsonNode).toEvent();
        }
    },

    DyeingSampleSubmitEvent {
        @Override
        public Single<DyeingSampleSubmitEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.DyeingSampleSubmitEvent.DTO.from(jsonNode).toEvent();
        }
    },

    TemporaryBoxEvent {
        @Override
        public Single<TemporaryBoxEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.TemporaryBoxEvent.DTO.from(jsonNode).toEvent();
        }
    },

    ToDtyEvent {
        @Override
        public Single<ToDtyEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.ToDtyEvent.DTO.from(jsonNode).toEvent();
        }
    },

    ToDtyConfirmEvent {
        @Override
        public Single<ToDtyConfirmEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.ToDtyConfirmEvent.DTO.from(jsonNode).toEvent();
        }
    },

    ProductProcessSubmitEvent {
        @Override
        public Single<ProductProcessSubmitEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.ProductProcessSubmitEvent.DTO.from(jsonNode).toEvent();
        }
    },

    ExceptionCleanEvent {
        @Override
        public Single<ExceptionCleanEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.ExceptionCleanEvent.DTO.from(jsonNode).toEvent();
        }
    },

    DyeingPrepareEvent {
        @Override
        public Single<DyeingPrepareEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.DyeingPrepareEvent.DTO.from(jsonNode).toEvent();
        }
    },

    PackageBoxEvent {
        @Override
        public Single<PackageBoxEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.PackageBoxEvent.DTO.from(jsonNode).toEvent();
        }
    },

    SmallPackageBoxEvent {
        @Override
        public Single<SmallPackageBoxEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SmallPackageBoxEvent.DTO.from(jsonNode).toEvent();
        }
    },

    PackageBoxFlipEvent {
        @Override
        public Single<PackageBoxFlipEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.PackageBoxFlipEvent.DTO.from(jsonNode).toEvent();
        }
    },

    SilkNoteFeedbackEvent {
        @Override
        public Single<SilkNoteFeedbackEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkNoteFeedbackEvent.DTO.from(jsonNode).toEvent();
        }
    },

    JikonAdapterSilkCarInfoFetchEvent {
        @Override
        public Single<JikonAdapterSilkCarInfoFetchEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkCarInfoFetchEvent.DTO.from(jsonNode).toEvent();
        }
    },

    JikonAdapterSilkDetachEvent {
        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkDetachEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkDetachEvent.DTO.from(jsonNode).toEvent();
        }
    },

    JikonAdapterPackageBoxEvent {
        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterPackageBoxEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterPackageBoxEvent.DTO.from(jsonNode).toEvent();
        }
    },

    RiambSilkCarInfoFetchEvent {
        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkCarInfoFetchEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkCarInfoFetchEvent.DTO.from(jsonNode).toEvent();
        }
    },

    RiambSilkDetachEvent {
        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkDetachEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkDetachEvent.DTO.from(jsonNode).toEvent();
        }
    },

    RiambPackageBoxEvent {
        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambPackageBoxEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambPackageBoxEvent.DTO.from(jsonNode).toEvent();
        }
    },

    WarehousePackageBoxFetchEvent {
        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent> from(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent.DTO.from(jsonNode).toEvent();
        }
    };

    public abstract <T extends EventSource> Single<T> from(JsonNode jsonNode);

}
