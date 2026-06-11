import client from './client'

export async function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const res = await client.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data
}
