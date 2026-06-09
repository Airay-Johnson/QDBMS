import request from './request'

export function listRespondents(params) {
  return request.get('/respondents', { params })
}

export function getRespondent(id) {
  return request.get(`/respondents/${id}`)
}

export function createRespondent(questionnaireId, data) {
  return request.post('/respondents', data, { params: { questionnaireId } })
}

export function updateRespondent(id, data) {
  return request.put(`/respondents/${id}`, data)
}

export function deleteRespondent(id) {
  return request.delete(`/respondents/${id}`)
}

export function importRespondents(questionnaireId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/respondents/import', formData, {
    params: { questionnaireId },
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
