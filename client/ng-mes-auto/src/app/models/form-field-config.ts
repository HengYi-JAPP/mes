export class FormFieldConfig {
  static ALL_VALUE_TYPE = ['STRING', 'BOOLEAN', 'NUMBER'];
  static ALLL_INPUT_TYPE = ['DEFAULT', 'SELECTION'];
  id: string;
  name: string;
  required = true;
  multi = false;
  valueType = FormFieldConfig.ALL_VALUE_TYPE[0];
  inputType = FormFieldConfig.ALLL_INPUT_TYPE[0];
  selectOptions: string[];

  get isInputString(): boolean {
    return this.valueType === 'STRING' && this.inputType === 'DEFAULT';
  }

  get isInputNumber(): boolean {
    return this.valueType === 'NUMBER' && this.inputType === 'DEFAULT';
  }

  get isInputBoolean(): boolean {
    return this.valueType === 'BOOLEAN';
  }

  get isSelectString(): boolean {
    return this.valueType === 'STRING' && this.inputType === 'SELECTION';
  }

  get isSelectNumber(): boolean {
    return this.valueType === 'NUMBER' && this.inputType === 'SELECTION';
  }

  static assign(...sources: any[]): FormFieldConfig {
    const result = Object.assign(new FormFieldConfig(), ...sources);
    return result;
  }

  static hasSelectOptions(valueType: string, inputType: string): boolean {
    if (valueType === 'ARRAY' || inputType === 'SELECTION') {
      return true;
    }
    return false;
  }
}
