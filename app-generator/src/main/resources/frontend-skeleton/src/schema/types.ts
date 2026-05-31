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

export type ItemDef =
  | TextItemDef
  | NumberItemDef
  | SelectItemDef
  | MultiSelectItemDef
  | TimeItemDef
  | OnOffItemDef
  | SecretItemDef

export interface FormDef {
  name: string
  title: string
  desc?: string
  items: ItemDef[]
  indices?: IndexDef[]
}

export interface MenuDef {
  group?: string
  icon?: string
  order?: number
  form: FormDef
}

export interface AppDef {
  namespace: string
  name: string
  title: string
  menus: MenuDef[]
}
