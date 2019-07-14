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
        public SilkCarRuntimeInitEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<SilkCarRuntimeInitEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },
    SilkCarRuntimeAppendEvent {
        @Override
        public SilkCarRuntimeAppendEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeAppendEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<SilkCarRuntimeAppendEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },
    BigSilkCarSilkChangeEvent {
        @Override
        public BigSilkCarSilkChangeEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.BigSilkCarSilkChangeEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<BigSilkCarSilkChangeEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    SilkCarRuntimeGradeEvent {
        @Override
        public SilkCarRuntimeGradeEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeGradeEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<SilkCarRuntimeGradeEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    SilkCarRuntimeWeightEvent {
        @Override
        public SilkCarRuntimeWeightEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeWeightEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<SilkCarRuntimeWeightEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    SilkRuntimeDetachEvent {
        @Override
        public SilkRuntimeDetachEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkRuntimeDetachEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<SilkRuntimeDetachEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    SilkCarRuntimeGradeSubmitEvent {
        @Override
        public SilkCarRuntimeGradeSubmitEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeGradeSubmitEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<SilkCarRuntimeGradeSubmitEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    DyeingSampleSubmitEvent {
        @Override
        public DyeingSampleSubmitEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.DyeingSampleSubmitEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<DyeingSampleSubmitEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    TemporaryBoxEvent {
        @Override
        public TemporaryBoxEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.TemporaryBoxEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<TemporaryBoxEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    ToDtyEvent {
        @Override
        public ToDtyEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.ToDtyEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<ToDtyEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    ToDtyConfirmEvent {
        @Override
        public ToDtyConfirmEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.ToDtyConfirmEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<ToDtyConfirmEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    ProductProcessSubmitEvent {
        @Override
        public ProductProcessSubmitEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.ProductProcessSubmitEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<ProductProcessSubmitEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    ExceptionCleanEvent {
        @Override
        public ExceptionCleanEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.ExceptionCleanEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<ExceptionCleanEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    DyeingPrepareEvent {
        @Override
        public DyeingPrepareEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.DyeingPrepareEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<DyeingPrepareEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    PackageBoxEvent {
        @Override
        public PackageBoxEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.PackageBoxEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<PackageBoxEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    SmallPackageBoxEvent {
        @Override
        public SmallPackageBoxEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SmallPackageBoxEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<SmallPackageBoxEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    PackageBoxFlipEvent {
        @Override
        public PackageBoxFlipEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.PackageBoxFlipEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<PackageBoxFlipEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    SilkNoteFeedbackEvent {
        @Override
        public SilkNoteFeedbackEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.application.event.SilkNoteFeedbackEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<SilkNoteFeedbackEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    JikonAdapterSilkCarInfoFetchEvent {
        @Override
        public JikonAdapterSilkCarInfoFetchEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkCarInfoFetchEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<JikonAdapterSilkCarInfoFetchEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    JikonAdapterSilkDetachEvent {
        @Override
        public com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkDetachEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkDetachEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterSilkDetachEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    JikonAdapterPackageBoxEvent {
        @Override
        public com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterPackageBoxEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterPackageBoxEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.jikon.event.JikonAdapterPackageBoxEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    RiambSilkCarInfoFetchEvent {
        @Override
        public com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkCarInfoFetchEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkCarInfoFetchEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkCarInfoFetchEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    RiambSilkDetachEvent {
        @Override
        public com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkDetachEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkDetachEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambSilkDetachEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    RiambPackageBoxEvent {
        @Override
        public com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambPackageBoxEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambPackageBoxEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.riamb.event.RiambPackageBoxEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    },

    WarehousePackageBoxFetchEvent {
        @Override
        public com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent.DTO toDto(JsonNode jsonNode) {
            return com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent.DTO.from(jsonNode);
        }

        @Override
        public Single<com.hengyi.japp.mes.auto.interfaces.warehouse.event.WarehousePackageBoxFetchEvent> from(JsonNode jsonNode) {
            return toDto(jsonNode).toEvent();
        }
    };

    public abstract <T extends EventSource.DTO> T toDto(JsonNode jsonNode);

    public abstract <T extends EventSource> Single<T> from(JsonNode jsonNode);

}
