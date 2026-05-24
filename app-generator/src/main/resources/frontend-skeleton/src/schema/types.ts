export type InitOrEditPattern = 'doNot' | 'userInput' | 'todo'
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
  initPattern: InitOrEditPattern
  editPattern: InitOrEditPattern
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
  group?: string
  icon?: string
  order?: number
  items: ItemDef[]
  indices?: IndexDef[]
}
