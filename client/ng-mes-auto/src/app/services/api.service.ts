import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {baseApiUrl} from '../../environments/environment';
import {AuthInfo} from '../models/auth-info';
import {Batch} from '../models/batch';
import {Corporation} from '../models/corporation';
import {EventSourceTypes} from '../models/event-source';
import {FormConfig} from '../models/form-config';
import {Grade} from '../models/grade';
import {Line} from '../models/line';
import {LineMachine} from '../models/line-machine';
import {LineMachineProductPlan} from '../models/line-machine-product-plan';
import {MeasureReport} from '../models/measure-report';
import {Operator} from '../models/operator';
import {OperatorGroup} from '../models/operator-group';
import {PackageClass} from '../models/package-class';
import {Permission} from '../models/permission';
import {Product} from '../models/product';
import {ProductPlanNotify} from '../models/product-plan-notify';
import {ProductProcess} from '../models/product-process';
import {SilkCar} from '../models/silk-car';
import {SilkCarRuntime} from '../models/silk-car-runtime';
import {SilkException} from '../models/silk-exception';
import {SilkNote} from '../models/silk-note';
import {StatisticsReport} from '../models/statistics-report';
import {SuggestOperator} from '../models/suggest-operator';
import {Workshop} from '../models/workshop';
import {WorkshopProductPlanReport} from '../models/workshop-product-plan-report';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient) {
  }

  workshopProductPlanReport(params?: HttpParams): Observable<WorkshopProductPlanReport> {
    return this.http.get<WorkshopProductPlanReport>(`${baseApiUrl}/reports/workshopProductPlanReport`, {params});
  }

  saveOperatorGroup(operatorGroup: OperatorGroup): Observable<OperatorGroup> {
    return operatorGroup.id ? this.updateOperatorGroup(operatorGroup) : this.createOperatorGroup(operatorGroup);
  }

  listOperatorGroup(params?: HttpParams): Observable<OperatorGroup[]> {
    return this.http.get<OperatorGroup[]>(`${baseApiUrl}/operatorGroups`, {params});
  }

  listSuggestOperator(params?: HttpParams): Observable<SuggestOperator[]> {
    return this.http.get<SuggestOperator[]>(`${baseApiUrl}/suggestOperators`, {params});
  }

  deletePermission(id: string): Observable<any> {
    return this.http.delete(`${baseApiUrl}/permissions/${id}`);
  }

  saveOperator(operator: Operator): Observable<Operator> {
    return operator.id ? this.updateOperator(operator) : this.importOperator(operator);
  }

  getOperator(id: string): Observable<Operator> {
    return this.http.get<Operator>(`${baseApiUrl}/operators/${id}`);
  }

  listOperator(params?: HttpParams): Observable<{ count: number, first: number, pageSize: number, operators: Operator[] }> {
    return this.http.get<{ count: number, first: number, pageSize: number, operators: Operator[] }>(`${baseApiUrl}/operators`, {params});
  }

  saveProductPlanNotify(productPlanNotify: ProductPlanNotify): Observable<ProductPlanNotify> {
    return productPlanNotify.id ? this.updateProductPlanNotify(productPlanNotify) : this.createProductPlanNotify(productPlanNotify);
  }

  getProductPlanNotify(id: string): Observable<ProductPlanNotify> {
    return this.http.get<ProductPlanNotify>(`${baseApiUrl}/productPlanNotifies/${id}`);
  }

  getProductPlanNotify_exeInfo(id: string): Observable<{ productPlanNotify: ProductPlanNotify, lineMachineProductPlans: LineMachineProductPlan[] }> {
    return this.http.get<{ productPlanNotify: ProductPlanNotify, lineMachineProductPlans: LineMachineProductPlan[] }>(`${baseApiUrl}/productPlanNotifies/${id}/exeInfo`);
  }

  exeProductPlanNotify(id: string, lineMachine: LineMachine): Observable<void> {
    return this.http.post<void>(`${baseApiUrl}/productPlanNotifies/${id}/exe`, {lineMachine});
  }

  batchExeProductPlanNotify(id: string, lineMachines: LineMachine[]): Observable<void> {
    return this.http.post<void>(`${baseApiUrl}/productPlanNotifies/${id}/batchExe`, {lineMachines});
  }

  finishProductPlanNotify(id: string): Observable<void> {
    return this.http.put<void>(`${baseApiUrl}/productPlanNotifies/${id}/finish`, null);
  }

  unFinishProductPlanNotify(id: string): Observable<void> {
    return this.http.delete<void>(`${baseApiUrl}/productPlanNotifies/${id}/finish`);
  }

  deleteProductPlanNotify(id: string): Observable<any> {
    return this.http.delete(`${baseApiUrl}/productPlanNotifies/${id}`);
  }

  listProductPlanNotify(params?: HttpParams): Observable<{ count: number, first: number, pageSize: number, productPlanNotifies: ProductPlanNotify[] }> {
    return this.http.get<{ count: number, first: number, pageSize: number, productPlanNotifies: ProductPlanNotify[] }>(`${baseApiUrl}/productPlanNotifies`, {params});
  }

  /*车间增删改查*/
  saveWorkshop(workshop: Workshop): Observable<Workshop> {
    return workshop.id ? this.updateWorkshop(workshop) : this.createWorkshop(workshop);
  }

  getWorkshop_Lines(id: string): Observable<Line[]> {
    return this.http.get<Line[]>(`${baseApiUrl}/workshops/${id}/lines`);
  }

  listWorkshop(params?: HttpParams): Observable<Workshop[]> {
    return this.http.get<Workshop[]>(`${baseApiUrl}/workshops`, {params});
  }

  deleteWorkshop(id: string): Observable<any> {
    return this.http.delete(`${baseApiUrl}/workshops/${id}`);
  }

  /*产品增删改查*/
  saveProduct(product: Product): Observable<Product> {
    return product.id ? this.updateProduct(product) : this.createProduct(product);
  }

  getProduct(id: string): Observable<Product> {
    return this.http.get<Product>(`${baseApiUrl}/products/${id}`);
  }

  getProduct_ProductProcess(id: string): Observable<ProductProcess[]> {
    return this.http.get<ProductProcess[]>(`${baseApiUrl}/products/${id}/productProcesses`);
  }

  listProduct(params?: HttpParams): Observable<Product[]> {
    return this.http.get<Product[]>(`${baseApiUrl}/products`, {params});
  }

  /*生产工艺的增删改查*/
  saveProductProcess(productProcess: ProductProcess): Observable<ProductProcess> {
    return productProcess.id ? this.updateProductProcess(productProcess) : this.createProductProcess(productProcess);
  }

  listProductProcess(params?: HttpParams): Observable<ProductProcess> {
    return this.http.get<ProductProcess>(`${baseApiUrl}/productProcesses`, {params});
  }

  /*生产工艺异常信息*/
  saveSilkException(silkException: SilkException): Observable<SilkException> {
    return silkException.id ? this.updateSilkException(silkException) : this.createSilkException(silkException);
  }

  listSilkException(params?: HttpParams): Observable<SilkException[]> {
    return this.http.get<SilkException[]>(`${baseApiUrl}/silkExceptions`, {params});
  }

  saveSilkNote(silkNote: SilkNote): Observable<SilkNote> {
    return silkNote.id ? this.updateSilkNote(silkNote) : this.createSilkNote(silkNote);
  }

  listSilkNote(params?: HttpParams): Observable<SilkNote[]> {
    return this.http.get<SilkNote[]>(`${baseApiUrl}/silkNotes`, {params});
  }

  /*线别*/
  saveLine(line: Line): Observable<Line> {
    return line.id ? this.updateLine(line) : this.createLine(line);
  }

  deleteLine(id: string): Observable<any> {
    return this.http.delete(`${baseApiUrl}/lines/${id}`);
  }

  listLine(params?: HttpParams): Observable<{ count: number, first: number, pageSize: number, lines: Line[] }> {
    return this.http.get<{ count: number, first: number, pageSize: number, lines: Line[] }>(`${baseApiUrl}/lines`, {params});
  }

  getLine_LineMachines(id: string): Observable<LineMachine[]> {
    return this.http.get<LineMachine []>(`${baseApiUrl}/lines/${id}/lineMachines`);
  }

  /*丝车*/
  saveSilkCar(silkCar: SilkCar): Observable<SilkCar> {
    return silkCar.id ? this.updateSilkCar(silkCar) : this.createSilkCar(silkCar);
  }

  batchSilkCars(silkCars: SilkCar[]): Observable<void> {
    return this.http.post<void>(`${baseApiUrl}/batchSilkCars`, silkCars);
  }

  deleteSilkCar(id: string): Observable<any> {
    return this.http.delete(`${baseApiUrl}/silkCars/${id}`);
  }

  getSilkCar(id: string): Observable<SilkCar> {
    return this.http.get<SilkCar>(`${baseApiUrl}/silkCars/${id}`);
  }

  getSilkCarByCode(code: string): Observable<SilkCar> {
    return this.http.get<SilkCar>(`${baseApiUrl}/silkCarCodes/${code}`);
  }

  listSilkCar(params?: HttpParams): Observable<{ count: number, first: number, pageSize: number, silkCars: SilkCar[] }> {
    return this.http.get<{ count: number, first: number, pageSize: number, silkCars: SilkCar [] }>(`${baseApiUrl}/silkCars`, {params});
  }

  autoCompleteSilkCar(q: string): Observable<SilkCar[]> {
    const params = new HttpParams().set('q', q);
    return this.http.get<SilkCar[]>(`${baseApiUrl}/autoComplete/silkCar`, {params});
  }

  autoCompleteFormConfig(q: string): Observable<FormConfig[]> {
    const params = new HttpParams().set('q', q);
    return this.http.get<FormConfig[]>(`${baseApiUrl}/autoComplete/formConfig`, {params});
  }

  saveFormConfig(formConfig: FormConfig): Observable<FormConfig> {
    return formConfig.id ? this.updateFormConfig(formConfig) : this.createFormConfig(formConfig);
  }

  /*批号*/
  saveBatch(batch: Batch): Observable<Batch> {
    return batch.id ? this.updateBatch(batch) : this.createBatch(batch);
  }

  listBatch(params?: HttpParams): Observable<{ count: number, first: number, pageSize: number, batches: Batch [] }> {
    return this.http.get<{ count: number, first: number, pageSize: number, batches: Batch[] }>(`${baseApiUrl}/batches`, {params});
  }

  deleteBatch(id: string): Observable<any> {
    return this.http.delete(`${baseApiUrl}/batches/${id}`);
  }

  /*公司*/
  listCorporation(params?: HttpParams): Observable<Corporation []> {
    return this.http.get<Corporation[]>(`${baseApiUrl}/corporations`, {params});
  }

  /*机台*/
  saveLineMachine(lineMachine: LineMachine): Observable<LineMachine> {
    return lineMachine.id ? this.updateLineMachine(lineMachine) : this.createLineMachine(lineMachine);
  }

  batchLineMachines(lineMachines: LineMachine[]): Observable<LineMachine[]> {
    return this.http.post<LineMachine[]>(`${baseApiUrl}/batchLineMachines`, lineMachines);
  }

  getLineMachine_ProductPlan(id: string): Observable<LineMachineProductPlan> {
    return this.http.get<LineMachineProductPlan>(`${baseApiUrl}/lineMachines/${id}/productPlan`);
  }

  listLineMachine(params?: HttpParams): Observable<LineMachine[]> {
    return this.http.get<LineMachine[]>(`${baseApiUrl}/lineMachines`, {params});
  }

  deleteLineMachine(id: string): Observable<any> {
    return this.http.delete(`${baseApiUrl}/lineMachines/${id}`);
  }

  /*等级*/
  saveGrade(grade: Grade): Observable<Grade> {
    return grade.id ? this.updateGrade(grade) : this.createGrade(grade);
  }

  listGrade(params?: HttpParams): Observable<Grade []> {
    return this.http.get<Grade[]>(`${baseApiUrl}/grades`, {params});
  }

  deleteGrade(id: string): Observable<any> {
    return this.http.delete(`${baseApiUrl}/grades/${id}`);
  }

  listPackageClass(params?: HttpParams): Observable<PackageClass []> {
    return this.http.get<PackageClass[]>(`${baseApiUrl}/packageClasses`, {params});
  }

  /*权限*/
  savePermission(permission: Permission): Observable<Permission> {
    return permission.id ? this.updatePermission(permission) : this.createPermission(permission);
  }

  listPermission(params?: HttpParams): Observable<Permission []> {
    return this.http.get<Permission[]>(`${baseApiUrl}/permissions`, {params});
  }

  authInfo(): Observable<AuthInfo> {
    return this.http.get<AuthInfo>(`${baseApiUrl}/auth`);
  }

  getSilkCarRuntimeByCode(code: string): Observable<SilkCarRuntime> {
    return this.http.get<SilkCarRuntime>(`${baseApiUrl}/silkCarRuntimes/${code}`);
  }

  productProcessSubmitEvents(event: EventSourceTypes): Observable<void> {
    return this.http.post<void>(`${baseApiUrl}/ProductProcessSubmitEvents`, event);
  }

  dyeingSampleSubmitEvents(event: EventSourceTypes): Observable<void> {
    return this.http.post<void>(`${baseApiUrl}/DyeingSampleSubmitEvents`, event);
  }

  getSilkException(id: string): Observable<SilkException> {
    return this.http.get<SilkException>(`${baseApiUrl}/silkExceptions/${id}`);
  }

  getSilkNote(id: string): Observable<SilkNote> {
    return this.http.get<SilkNote>(`${baseApiUrl}/silkNotes/${id}`);
  }

  getProductProcess(id: string): Observable<ProductProcess> {
    return this.http.get<ProductProcess>(`${baseApiUrl}/productProcesses/${id}`);
  }

  measureReport(params?: HttpParams): Observable<MeasureReport> {
    return this.http.get<MeasureReport>(`${baseApiUrl}/reports/measureReport`, {params});
  }

  statisticsReport(params?: HttpParams): Observable<StatisticsReport> {
    return this.http.get<StatisticsReport>(`${baseApiUrl}/reports/statisticsReport`, {params});
  }

  private updateProductPlanNotify(productPlanNotify: ProductPlanNotify): Observable<ProductPlanNotify> {
    return this.http.put<ProductPlanNotify>(`${baseApiUrl}/productPlanNotifies/${productPlanNotify.id}`, productPlanNotify);
  }

  private createProductPlanNotify(productPlanNotify: ProductPlanNotify): Observable<ProductPlanNotify> {
    return this.http.post<ProductPlanNotify>(`${baseApiUrl}/productPlanNotifies`, productPlanNotify);
  }

  private updateSilkException(silkException: SilkException): Observable<SilkException> {
    return this.http.put<SilkException>(`${baseApiUrl}/silkExceptions/${silkException.id}`, silkException);
  }

  private createSilkException(silkException: SilkException): Observable<SilkException> {
    return this.http.post<SilkException>(`${baseApiUrl}/silkExceptions`, silkException);
  }

  private updateSilkNote(silkNote: SilkNote): Observable<SilkNote> {
    return this.http.put<SilkNote>(`${baseApiUrl}/silkNotes/${silkNote.id}`, silkNote);
  }

  private createSilkNote(silkNote: SilkNote): Observable<SilkNote> {
    return this.http.post<SilkNote>(`${baseApiUrl}/silkNotes`, silkNote);
  }

  private updateLine(line: Line): Observable<Line> {
    return this.http.put<Line>(`${baseApiUrl}/lines/${line.id}`, line);
  }

  private createLine(line: Line): Observable<Line> {
    return this.http.post<Line>(`${baseApiUrl}/lines`, line);
  }

  private updateSilkCar(silkCar: SilkCar): Observable<SilkCar> {
    return this.http.put<SilkCar>(`${baseApiUrl}/silkCars/${silkCar.id}`, silkCar);
  }

  private createSilkCar(silkCar: SilkCar): Observable<SilkCar> {
    return this.http.post<SilkCar>(`${baseApiUrl}/silkCars`, silkCar);
  }

  private updateFormConfig(formConfig: FormConfig): Observable<FormConfig> {
    return this.http.put<FormConfig>(`${baseApiUrl}/formConfigs/${formConfig.id}`, formConfig);
  }

  private createFormConfig(formConfig: FormConfig): Observable<FormConfig> {
    return this.http.post<FormConfig>(`${baseApiUrl}/formConfigs`, formConfig);
  }

  private updateBatch(batch: Batch): Observable<Batch> {
    return this.http.put<Batch>(`${baseApiUrl}/batches/${batch.id}`, batch);
  }

  private createBatch(batch: Batch): Observable<Batch> {
    return this.http.post<Batch>(`${baseApiUrl}/batches`, batch);
  }

  private updateLineMachine(lineMachine: LineMachine): Observable<LineMachine> {
    return this.http.put<LineMachine>(`${baseApiUrl}/lineMachines/${lineMachine.id}`, lineMachine);
  }

  private createLineMachine(lineMachine: LineMachine): Observable<LineMachine> {
    return this.http.post<LineMachine>(`${baseApiUrl}/lineMachines`, lineMachine);
  }

  private updateGrade(grade: Grade): Observable<Grade> {
    return this.http.put<Grade>(`${baseApiUrl}/grades/${grade.id}`, grade);
  }

  private createGrade(grade: Grade): Observable<Grade> {
    return this.http.post<Grade>(`${baseApiUrl}/grades`, grade);
  }

  private updateOperator(operator: Operator): Observable<Operator> {
    return this.http.put<Operator>(`${baseApiUrl}/operators/${operator.id}`, operator);
  }

  private importOperator(operator: Operator): Observable<Operator> {
    return this.http.post<Operator>(`${baseApiUrl}/operators`, operator);
  }

  private updateOperatorGroup(operatorGroup: OperatorGroup): Observable<OperatorGroup> {
    return this.http.put<OperatorGroup>(`${baseApiUrl}/operatorGroups/${operatorGroup.id}`, operatorGroup);
  }

  private createOperatorGroup(operatorGroup: OperatorGroup): Observable<OperatorGroup> {
    return this.http.post<OperatorGroup>(`${baseApiUrl}/operatorGroups`, operatorGroup);
  }

  private updatePermission(permission: Permission): Observable<Permission> {
    return this.http.put<Permission>(`${baseApiUrl}/permissions/${permission.id}`, permission);
  }

  private createPermission(permission: Permission): Observable<Permission> {
    return this.http.post<Permission>(`${baseApiUrl}/permissions`, permission);
  }

  private updateWorkshop(workshop: Workshop): Observable<Workshop> {
    return this.http.put<Workshop>(`${baseApiUrl}/workshops/${workshop.id}`, workshop);
  }

  private createWorkshop(workshop: Workshop): Observable<Workshop> {
    return this.http.post<Workshop>(`${baseApiUrl}/workshops`, workshop);
  }

  private updateProduct(product: Product): Observable<Product> {
    return this.http.put<Product>(`${baseApiUrl}/products`, product);
  }

  private createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(`${baseApiUrl}/products`, product);
  }

  private updateProductProcess(productProcess: ProductProcess): Observable<ProductProcess> {
    return this.http.put<ProductProcess>(`${baseApiUrl}/productProcesses/${productProcess.id}`, productProcess);
  }

  private createProductProcess(productProcess: ProductProcess): Observable<ProductProcess> {
    return this.http.post<ProductProcess>(`${baseApiUrl}/productProcesses`, productProcess);
  }

}
