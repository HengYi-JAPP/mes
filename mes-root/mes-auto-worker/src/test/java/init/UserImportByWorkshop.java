package init;

import com.github.ixtf.japp.codec.Jcodec;
import com.github.ixtf.japp.core.J;
import com.google.common.collect.Lists;
import com.hengyi.japp.mes.auto.domain.OperatorGroup;
import com.hengyi.japp.mes.auto.repository.LoginRepository;
import com.hengyi.japp.mes.auto.repository.OperatorGroupRepository;
import com.hengyi.japp.mes.auto.repository.OperatorRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static init.UserImport.getString;
import static init.UserImport.injector;

/**
 * @author jzb 2018-11-08
 */
@Slf4j
public class UserImportByWorkshop {

    @SneakyThrows
    public static void main(String[] args) {
        final List<UserXlsxItem> userXlsx = UserXlsxItem.load("/home/mes/init/工厂人员资料.xlsx");
        importUser(userXlsx).subscribe(() -> System.out.println("===finish==="));
        TimeUnit.DAYS.sleep(1);
    }

    private static Completable importUser(List<UserXlsxItem> data) {
        final OperatorRepository operatorRepository = injector.getInstance(OperatorRepository.class);
        final OperatorGroupRepository operatorGroupRepository = injector.getInstance(OperatorGroupRepository.class);
        final LoginRepository loginRepository = injector.getInstance(LoginRepository.class);

        return operatorGroupRepository.list().toMap(OperatorGroup::getName).flatMapCompletable(operatorGroupMap -> Flowable.fromIterable(data).flatMapCompletable(it -> operatorRepository.create().flatMapCompletable(operator -> {
            operator.setName(it.name);
            final OperatorGroup operatorGroup = operatorGroupMap.get(it.oldRoleName);
            if (operatorGroup != null) {
                operator.setGroups(Lists.newArrayList(operatorGroup));
            }
//            if (Util.isHr(it.loginId)) {
//                final HrUserAd hrUserAd = adService.findByHrId(it.loginId);
//                if (hrUserAd != null) {
//                    operator.setHrId(hrUserAd.getHrId());
//                    operator.setName(hrUserAd.getDisplayName());
//                    return operatorRepository.save(operator).toCompletable();
//                }
//            }
            operator.setHrId(it.loginId);
            return loginRepository.create().flatMapCompletable(login -> {
                login.setLoginId(it.loginId);
                login.setPassword(Jcodec.password());
                return operatorRepository.save(operator).flatMapCompletable(o -> {
                    login.setOperator(o);
                    return loginRepository.save(login).ignoreElement();
                });
            });
        })));
    }

    @Data
    public static class UserXlsxItem {
        private String name;
        private String loginId;
        private String oldRoleName;

        @SneakyThrows
        private static List<UserXlsxItem> load(String path) {
            @Cleanup final Workbook wb = new XSSFWorkbook(path);
            final Sheet sheet = wb.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .map(row -> {
                        final UserXlsxItem item = new UserXlsxItem();
                        final String name = getString(row, 0);
                        final String loginId = getString(row, 1);
                        final String oldRoleName = getString(row, 2);
                        item.setName(J.deleteWhitespace(name));
                        item.setLoginId(J.deleteWhitespace(loginId));
                        item.setOldRoleName(J.deleteWhitespace(oldRoleName));
                        return item;
                    })
                    .collect(Collectors.toList());
        }
    }
}
