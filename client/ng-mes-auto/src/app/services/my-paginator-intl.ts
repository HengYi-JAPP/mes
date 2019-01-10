import {Injectable} from '@angular/core';
import {MatPaginatorIntl} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {Subject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MyPaginatorIntl extends MatPaginatorIntl {
  changes = new Subject<void>();

  constructor(private translate: TranslateService) {
    super();
    translate.get(['PaginatorIntl.itemsPerPageLabel', 'PaginatorIntl.nextPageLabel', 'PaginatorIntl.previousPageLabel', 'PaginatorIntl.firstPageLabel', 'PaginatorIntl.lastPageLabel'])
      .subscribe(it => {
        this.itemsPerPageLabel = it['PaginatorIntl.itemsPerPageLabel'];
        this.nextPageLabel = it['PaginatorIntl.nextPageLabel'];
        this.previousPageLabel = it['PaginatorIntl.previousPageLabel'];
        this.firstPageLabel = it['PaginatorIntl.firstPageLabel'];
        this.lastPageLabel = it['PaginatorIntl.lastPageLabel'];
        this.changes.next();
      });
  }
}
