import {FormConfig} from './form-config';
import {Product} from './product';
import {SilkException} from './silk-exception';
import {SilkNote} from './silk-note';

export class ProductProcess {
  id: string;
  name: string;
  sortBy: number;
  product: Product;
  exceptions: SilkException[];
  relateRoles: string[];
  notes: SilkNote[];
  formConfig: FormConfig;

  static assign(...sources: any[]): ProductProcess {
    const result: ProductProcess = Object.assign(new ProductProcess(), ...sources);
    if (result.formConfig) {
      result.formConfig = FormConfig.assign(result.formConfig);
    }
    return result;
  }

  static toEntities(os: ProductProcess[], entities?: { [id: string]: ProductProcess }): { [id: string]: ProductProcess } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = ProductProcess.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
