// core/fields/file-category.ts
// Frontend mirror of backend FileCategoryEnum. Used to generate <input accept>
// attributes and hint text, and to decide inline-preview branching.

const EXTENSIONS: Record<string, string[]> = {
  image: ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'],
  document: ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'csv', 'md'],
  archive: ['zip', 'rar', '7z', 'tar', 'gz'],
  audio: ['mp3', 'wav', 'flac', 'aac', 'ogg'],
  video: ['mp4', 'avi', 'mov', 'mkv', 'webm'],
}

const DANGEROUS_EXTENSIONS = new Set([
  'exe', 'bat', 'cmd', 'sh', 'js', 'jar', 'msi', 'com', 'scr', 'vbs', 'dll', 'app',
])

const CATEGORY_TITLES: Record<string, string> = {
  image: '图片',
  document: '文档',
  archive: '压缩包',
  audio: '音频',
  video: '视频',
  general: '普通文件',
}

export function extractExt(fileName: string | undefined | null): string | null {
  if (!fileName) {
    return null
  }
  const dot = fileName.lastIndexOf('.')
  if (dot < 0 || dot === fileName.length - 1) {
    return null
  }
  return fileName.substring(dot + 1).toLowerCase()
}

export function acceptOf(category: string | undefined): string {
  const cat = category || 'general'
  if (cat === 'general') {
    return ''
  }
  const exts = EXTENSIONS[cat]
  if (!exts) {
    return ''
  }
  return exts.map((e) => '.' + e).join(',')
}

export function isExtensionAllowed(category: string | undefined, fileName: string): boolean {
  const cat = category || 'general'
  const ext = extractExt(fileName)
  if (!ext) {
    return false
  }
  if (cat === 'general') {
    return !DANGEROUS_EXTENSIONS.has(ext)
  }
  const exts = EXTENSIONS[cat]
  return !!exts && exts.includes(ext)
}

export function hintOf(category: string | undefined, maxFileSize?: number): string {
  const cat = category || 'general'
  const title = CATEGORY_TITLES[cat] || '普通文件'
  const parts: string[] = [`类别：${title}`]
  if (cat !== 'general') {
    parts.push(`支持：${(EXTENSIONS[cat] || []).join('、')}`)
  }
  if (maxFileSize) {
    parts.push(`大小上限：${maxFileSize}MB`)
  }
  return parts.join('；')
}

export function isPreviewableImage(fileName: string | undefined | null): boolean {
  const ext = extractExt(fileName)
  return !!ext && ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'].includes(ext)
}

export function isPreviewablePdf(fileName: string | undefined | null): boolean {
  return extractExt(fileName) === 'pdf'
}
