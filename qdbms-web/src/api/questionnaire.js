import request from './request'

export function createQuestionnaire(data) {
  return request.post('/questionnaires', data)
}

export function listQuestionnaires() {
  return request.get('/questionnaires')
}

export function getQuestionnaire(id) {
  return request.get(`/questionnaires/${id}`)
}

export function updateQuestionnaire(id, data) {
  return request.put(`/questionnaires/${id}`, data)
}

export function deleteQuestionnaire(id) {
  return request.delete(`/questionnaires/${id}`)
}

export function publishQuestionnaire(id) {
  return request.post(`/questionnaires/${id}/publish`)
}

export function closeQuestionnaire(id) {
  return request.post(`/questionnaires/${id}/close`)
}

// 问题管理
export function createQuestion(questionnaireId, data) {
  return request.post(`/questions`, data, { params: { questionnaireId } })
}

export function listQuestions(questionnaireId) {
  return request.get('/questions', { params: { questionnaireId } })
}

export function updateQuestion(id, data) {
  return request.put(`/questions/${id}`, data)
}

export function deleteQuestion(id) {
  return request.delete(`/questions/${id}`)
}

export function sortQuestions(questionIds) {
  return request.put('/questions/sort', questionIds)
}
