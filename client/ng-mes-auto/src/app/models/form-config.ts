import {FormFieldConfig} from './form-field-config';

export class FormConfig {
  id: string;
  name: string;
  formFieldConfigs: FormFieldConfig[];

  static assign(...sources: any[]): FormConfig {
    const result: FormConfig = Object.assign(new FormConfig(), ...sources);
    result.formFieldConfigs = (result.formFieldConfigs || []).map(it => FormFieldConfig.assign(it));
    return result;
  }

  static toEntities(os: FormConfig[], entities?: { [id: string]: FormConfig }): { [id: string]: FormConfig } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = FormConfig.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
