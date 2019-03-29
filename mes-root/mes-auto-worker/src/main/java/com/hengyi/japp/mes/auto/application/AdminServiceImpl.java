package com.hengyi.japp.mes.auto.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.exception.JAuthorizationException;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hengyi.japp.mes.auto.application.event.SilkCarRuntimeInitEvent;
import com.hengyi.japp.mes.auto.domain.SilkCar;
import com.hengyi.japp.mes.auto.domain.SilkCarRuntime;
import com.hengyi.japp.mes.auto.domain.SilkRuntime;
import com.hengyi.japp.mes.auto.domain.data.DoffingType;
import com.hengyi.japp.mes.auto.dto.CheckSilkDTO;
import com.hengyi.japp.mes.auto.repository.GradeRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import com.hengyi.japp.mes.auto.repository.PackageBoxRepository;
import com.hengyi.japp.mes.auto.repository.SilkCarRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.List;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-22
 */
@Slf4j
@Singleton
public class AdminServiceImpl implements AdminService {
    private final SilkCarRuntimeService silkCarRuntimeService;
    private final SilkCarRepository silkCarRepository;
    private final GradeRepository gradeRepository;
    private final PackageBoxRepository packageBoxRepository;
    private final OperatorRepository operatorRepository;

    @Inject
    private AdminServiceImpl(SilkCarRuntimeService silkCarRuntimeService, SilkCarRepository silkCarRepository, GradeRepository gradeRepository, PackageBoxRepository packageBoxRepository, OperatorRepository operatorRepository) {
        this.silkCarRuntimeService = silkCarRuntimeService;
        this.silkCarRepository = silkCarRepository;
        this.gradeRepository = gradeRepository;
        this.packageBoxRepository = packageBoxRepository;
        this.operatorRepository = operatorRepository;
    }

    private Completable rxCheckAdmin(Principal principal) {
        return operatorRepository.find(principal).flatMapCompletable(operator -> {
            if (operator.isAdmin()) {
                return Completable.complete();
            }
            throw new JAuthorizationException();
        });
    }

    @Override
    public Single<SilkCarRuntime> handle(Principal principal, SilkCarRuntimeInitEvent.AdminManualDoffingCommand command) {
        final SilkCarRuntimeInitEvent event = new SilkCarRuntimeInitEvent();
        event.setCommand(MAPPER.convertValue(command, JsonNode.class));
        final Single<SilkCarRuntime> result$ = silkCarRepository.findByCode(command.getSilkCar().getCode()).flatMap(silkCar -> {
            event.setSilkCar(silkCar);
            final AdminManualSilkCarModel silkCarModel = new AdminManualSilkCarModel(silkCar, command.getCheckSilks().size());
            return silkCarModel.generateSilkRuntimes(command.getCheckSilks());
        }).flatMap(silkRuntimes -> {
            event.setSilkRuntimes(silkRuntimes);
            return gradeRepository.find(command.getGrade().getId());
        }).flatMap(grade -> {
            event.setGrade(grade);
            return operatorRepository.find(principal);
        }).flatMap(operator -> {
            event.fire(operator);
            return silkCarRuntimeService.doffing(event, DoffingType.MANUAL);
        });
        return rxCheckAdmin(principal).andThen(result$);
    }

    private class AdminManualSilkCarModel extends ManualSilkCarModel {
        private AdminManualSilkCarModel(SilkCar silkCar, float count) {
            super(silkCar, count);
        }

        @Override
        public Single<List<SilkRuntime>> generateSilkRuntimes(List<CheckSilkDTO> checkSilks) {
            return toSilkBarcodes(checkSilks)
                    .flatMap(it -> generateSilkRuntimesBySilkBarcodes(ImmutableList.builder(), it))
                    .map(it -> checkPosition(it, checkSilks));
        }
    }

    @Override
    public Completable unlockSilkBarcodeRepositoryMongo(Principal principal) {
//        SilkBarcodeRepositoryMongo.semaphore.release();
        return Completable.complete();
    }
}
