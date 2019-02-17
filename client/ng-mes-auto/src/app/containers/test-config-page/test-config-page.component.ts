import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';
import {Line} from '../../models/line';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {Search} from '../../store/actions/measure-report-page';

@Component({
  templateUrl: './test-config-page.component.html',
  styleUrls: ['./test-config-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TestConfigPageComponent {
  @HostBinding('class.app-page') readonly b1 = true;
  @HostBinding('class.measure-report-page') readonly b2 = true;
  readonly displayedColumns = ['workshop', 'name', 'doffingType', 'btns'];
  readonly searchForm: FormGroup;
  readonly workshops$ = this.apiService.listWorkshop();
  readonly workshopId$: Observable<string>;
  readonly reportItems$: Observable<string>;

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private router: Router,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
    this.searchForm = this.fb.group({
      'workshopId': '',
      'startDate': new Date(),
      'endDate': new Date(),
    });
  }

  search() {
    this.store.dispatch(new Search(this.searchForm.value));
  }

  export() {
  }

  detail(line: Line) {
  }
}
