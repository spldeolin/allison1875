// core/protocol/file-api.ts
// Client for the backend's generic file upload/download endpoints.

import request from '@/utils/request'
import type { FileValue } from '@/schema/types'

interface UploadFileResp {
  fileKey: string
  originFileName: string
}

interface TemporarilyDownloadFileResp {
  token: string
}

/** Upload a file under a category, returns the stored FileValue. */
export async function uploadFile(file: File, category: string): Promise<FileValue> {
  const form = new FormData()
  form.append('file', file)
  form.append('category', category)
  const res = await request.post('/api/v1/file/uploadFile', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  const data = res.data.data as UploadFileResp
  return { fileKey: data.fileKey, originFileName: data.originFileName }
}

/** Request a stateless HMAC download token for a fileKey. */
export async function fetchDownloadToken(fileKey: string): Promise<string> {
  const res = await request.post('/api/v1/file/temporarilyDownloadFile', null, {
    params: { fileKey },
  })
  return (res.data.data as TemporarilyDownloadFileResp).token
}

/** Build the anonymous download URL carrying a signed token. */
export function downloadUrlOf(token: string): string {
  return '/api/v1/file/downloadFile?token=' + encodeURIComponent(token)
}
