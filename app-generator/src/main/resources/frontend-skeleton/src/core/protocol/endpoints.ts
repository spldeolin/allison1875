// protocol/endpoints.ts
// URL derivation rules — sourced from contract.md §1-2
// All endpoints use POST method.
//
// URL pattern:
//   base  = /api/v1/${lowerCamelFormName}
//   list      = base/list${FormName}s
//   save      = base/save${FormName}
//   getDetail = base/get${FormName}Detail
//   delete    = base/delete${FormName}

import { upperCamelToLowerCamel } from '@/utils/naming'

export type CrudAction = 'list' | 'save' | 'delete' | 'getDetail'

export function endpointOf(formName: string, action: CrudAction): string {
  const lower = upperCamelToLowerCamel(formName)
  const base = `/api/v1/${lower}`
  switch (action) {
    case 'list':
      return `${base}/list${formName}s`
    case 'save':
      return `${base}/save${formName}`
    case 'getDetail':
      return `${base}/get${formName}Detail`
    case 'delete':
      return `${base}/delete${formName}`
  }
}
