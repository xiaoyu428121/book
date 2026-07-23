import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function getBookReviewList(params) {
  return request({
    url: '/admin/books/review',
    method: 'get',
    params
  })
}

export function approveBook(id) {
  return request({
    url: `/admin/books/${id}/approve`,
    method: 'put'
  })
}

export function rejectBook(id) {
  return request({
    url: `/admin/books/${id}/reject`,
    method: 'put'
  })
}

export function getOrderList(params) {
  return request({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

export function getStatistics() {
  return request({
    url: '/admin/statistics',
    method: 'get'
  })
}
