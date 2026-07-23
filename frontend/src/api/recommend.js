import request from '@/utils/request'

export function getHotBooks() {
  return request({
    url: '/recommend/hot',
    method: 'get'
  })
}

export function getBooksByCategory() {
  return request({
    url: '/recommend/category',
    method: 'get'
  })
}

// 新增：【物品协同过滤】买了这本书的人还买了…
export function getSimilarBooks(bookId, limit = 6) {
  return request({
    url: `/recommend/similar/${bookId}`,
    method: 'get',
    params: { limit }
  })
}

// 新增：【用户协同过滤】猜你喜欢
export function getRecommendForUser(userId, limit = 8) {
  return request({
    url: `/recommend/for-user/${userId}`,
    method: 'get',
    params: { limit }
  })
}