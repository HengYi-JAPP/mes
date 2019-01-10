export class ProductType {
  id: string;
  name: string;

  static assign(...sources: any[]): ProductType {
    const result = Object.assign(new ProductType(), ...sources);
    return result;
  }

  static toEntities(os: ProductType[], entities?: { [id: string]: ProductType }): { [id: string]: ProductType } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = ProductType.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
