export function upperCamelToKebab(str: string): string {
  return str
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1-$2')
    .replace(/([a-z\d])([A-Z])/g, '$1-$2')
    .toLowerCase()
}

export function upperCamelToLowerCamel(str: string): string {
  return str.charAt(0).toLowerCase() + str.slice(1)
}

/**
 * Pluralize an UpperCamelCase word using standard English rules.
 * Mirrors the pluralization used by form-generator's backend endpoint naming.
 *
 * Rules (applied to the last word in the compound):
 *   - ends in s/x/z/ch/sh  → +es   (e.g. Box → Boxes, Watch → Watches)
 *   - ends in consonant+y  → -y+ies (e.g. Dormitory → Dormitories, Category → Categories)
 *   - ends in fe           → -fe+ves (e.g. Wife → Wives)
 *   - ends in f            → -f+ves  (e.g. Leaf → Leaves)
 *   - everything else      → +s
 */
export function pluralize(word: string): string {
  if (!word) return word
  if (/(?:s|x|z|ch|sh)$/i.test(word)) return word + 'es'
  if (/[^aeiou]y$/i.test(word)) return word.slice(0, -1) + 'ies'
  if (/fe$/i.test(word)) return word.slice(0, -2) + 'ves'
  if (/f$/i.test(word)) return word.slice(0, -1) + 'ves'
  return word + 's'
}
