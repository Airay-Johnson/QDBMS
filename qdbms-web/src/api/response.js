import request from './request'

export function submitResponse(questionnaireId, answers) {
  return request.post('/responses', answers, { params: { questionnaireId } })
}

export function listResponses(questionnaireId) {
  return request.get('/responses', { params: { questionnaireId } })
}
