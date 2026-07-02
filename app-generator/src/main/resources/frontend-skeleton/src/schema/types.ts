export type FilterPattern =
  | 'in'
  | 'ge'
  | 'gt'
  | 'le'
  | 'lt'
  | 'like'
  | 'dateRange'
  | 'dateTimeRange'
export type TimeFormat = 'date' | 'time' | 'dateTime'

export interface OptionDef {
  code: string
  title: string
}

export interface IndexDef {
  itemNames: string[]
  isUnique: boolean
}

interface ItemDefBase {
  name: string
  title: string
  isNonVoid: boolean
  /** 创建时是否允许用户输入。默认 true。 */
  canInputOnInit?: boolean
  /** 编辑时是否允许用户输入。默认 true。 */
  canInputOnEdit?: boolean
}

export interface TextItemDef extends ItemDefBase {
  type: 'text'
  maxLength?: number
  isMultilineOrRich?: boolean
  regex?: string
}

export interface NumberItemDef extends ItemDefBase {
  type: 'number'
  canBeDecimal?: boolean
}

export interface SelectItemDef extends ItemDefBase {
  type: 'select'
  options: OptionDef[]
}

export interface MultiSelectItemDef extends ItemDefBase {
  type: 'multiSelect'
  options: OptionDef[]
}

export interface TimeItemDef extends ItemDefBase {
  type: 'time'
  format: TimeFormat
}

export interface OnOffItemDef extends ItemDefBase {
  type: 'onOff'
}

export interface SecretItemDef extends ItemDefBase {
  type: 'secret'
}

/**
 * 文件类字段。业务表合并为单列 VARCHAR(512)，值为 "fileKey/originFileName"。
 * 表单 state 中此字段持有 FileValue 对象，提交时 join、取回时按首个 '/' split。
 */
export interface FileItemDef extends ItemDefBase {
  type: 'file'
  /** 文件类别，对应后端 FileCategoryEnum，默认 general */
  category?: string
  /** 前端 UX 校验用的最大文件大小（MB），留空不限制 */
  maxFileSize?: number
}

/** 文件字段在表单 state 中的值 */
export interface FileValue {
  fileKey: string
  originFileName: string
}

export type ItemDef =
  | TextItemDef
  | NumberItemDef
  | SelectItemDef
  | MultiSelectItemDef
  | TimeItemDef
  | OnOffItemDef
  | SecretItemDef
  | FileItemDef

export interface FormDef {
  name: string
  title: string
  desc?: string
  items: ItemDef[]
  indices?: IndexDef[]
}

export interface PermissionsDef {
  list: string
  create: string
  update: string
  delete: string
}

export interface MenuDef {
  group?: string
  icon?: string
  order?: number
  form: FormDef
  permissions?: PermissionsDef
}

export interface AppDef {
  namespace: string
  name: string
  title: string
  menus: MenuDef[]
}
