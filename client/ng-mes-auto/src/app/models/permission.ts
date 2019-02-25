export const ROLES = ['DOFFING', 'SUBMIT_DYEING_PREPARE', 'SUBMIT_DYEING_RESULT', 'SUBMIT_GRADE', 'SUBMIT_DYEING_SAMPLE'];

export class Permission {
  id: string;
  code: string;
  name: string;

  static assign(...sources: any[]): Permission {
    const result = Object.assign(new Permission(), ...sources);
    return result;
  }

  static toEntities(os: Permission[], entities?: { [id: string]: Permission }): { [id: string]: Permission } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = Permission.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
