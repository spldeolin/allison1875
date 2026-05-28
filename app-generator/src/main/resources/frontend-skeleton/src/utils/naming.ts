export function upperCamelToKebab(str: string): string {
  return str
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1-$2')
    .replace(/([a-z\d])([A-Z])/g, '$1-$2')
    .toLowerCase()
}

export function upperCamelToLowerCamel(str: string): string {
  return str.charAt(0).toLowerCase() + str.slice(1)
}
