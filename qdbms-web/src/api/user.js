import request from './request'

export function listUsers(params) {
  return request.get('/users', { params })
}

export function getUser(id) {
  return request.get(`/users/${id}`)
}

export function updateUser(id, params) {
  return request.put(`/users/${id}`, null, { params })
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function assignRoles(userId, roleIds) {
  return request.put(`/users/${userId}/roles`, roleIds)
}

export function getUserRoles(userId) {
  return request.get(`/users/${userId}/roles`)
}

export function listRoles() {
  return request.get('/roles')
}
