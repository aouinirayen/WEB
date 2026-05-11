/**
 * Converts snake_case keys to camelCase
 * This handles API responses that might return snake_case properties
 */
export function convertSnakeToCamel(obj: any): any {
  if (obj === null || obj === undefined) {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map(item => convertSnakeToCamel(item));
  }

  if (obj instanceof Date || obj instanceof RegExp) {
    return obj;
  }

  if (typeof obj !== 'object') {
    return obj;
  }

  const converted: any = {};
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      const camelKey = key.replace(/_([a-z])/g, (match, letter) => letter.toUpperCase());
      const value = obj[key];
      converted[camelKey] = typeof value === 'object' ? convertSnakeToCamel(value) : value;
    }
  }

  return converted;
}
