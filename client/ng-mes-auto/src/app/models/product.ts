export class Product {
  id: string;
  name: string;

  static assign(...sources: any[]): Product {
    const result = Object.assign(new Product(), ...sources);
    return result;
  }

  static toEntities(os: Product[], entities?: { [id: string]: Product }): { [id: string]: Product } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = Product.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
