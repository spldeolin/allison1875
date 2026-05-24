import fs from 'fs'
import path from 'path'
import yaml from 'js-yaml'
import type { Plugin } from 'vite'
import type { FormDef } from '../src/schema/types'

const VIRTUAL_MODULE_ID = 'virtual:form-dsl'
const RESOLVED_VIRTUAL_MODULE_ID = '\0' + VIRTUAL_MODULE_ID

export function parseDslFiles(dslDir: string): FormDef[] {
  if (!fs.existsSync(dslDir)) {
    return []
  }
  const files = fs.readdirSync(dslDir).filter(f => f.endsWith('.yml') || f.endsWith('.yaml'))
  const allForms: FormDef[] = []

  for (const file of files) {
    const content = fs.readFileSync(path.join(dslDir, file), 'utf-8')
    const parsed = yaml.load(content) as { forms?: FormDef[] }
    if (parsed && parsed.forms) {
      allForms.push(...parsed.forms)
    }
  }
  return allForms
}

export default function formDslPlugin(dslDir: string): Plugin {
  return {
    name: 'vite-plugin-form-dsl',
    resolveId(id) {
      if (id === VIRTUAL_MODULE_ID) {
        return RESOLVED_VIRTUAL_MODULE_ID
      }
    },
    load(id) {
      if (id === RESOLVED_VIRTUAL_MODULE_ID) {
        const forms = parseDslFiles(dslDir)
        return `export default ${JSON.stringify(forms)}`
      }
    },
    handleHotUpdate({ file, server }) {
      if (file.startsWith(dslDir) && (file.endsWith('.yml') || file.endsWith('.yaml'))) {
        const module = server.moduleGraph.getModuleById(RESOLVED_VIRTUAL_MODULE_ID)
        if (module) {
          server.moduleGraph.invalidateModule(module)
          server.ws.send({ type: 'full-reload' })
        }
      }
    }
  }
}
