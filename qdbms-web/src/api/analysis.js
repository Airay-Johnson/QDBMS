import request from './request'

export function getStats(questionnaireId) {
  return request.get(`/analysis/questionnaire/${questionnaireId}`)
}

export function getSingleChoiceStats(questionId) {
  return request.get(`/analysis/questions/${questionId}/single`)
}

export function getMultipleChoiceStats(questionId) {
  return request.get(`/analysis/questions/${questionId}/multiple`)
}

export function getTextAnswers(questionId) {
  return request.get(`/analysis/questions/${questionId}/text`)
}

export function getExportUrl(questionnaireId) {
  return `/api/export/questionnaire/${questionnaireId}`
}
