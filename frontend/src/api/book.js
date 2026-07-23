import request from '@/utils/request'

// 获取书籍列表（分页/筛选）
export function getBookList(params) {
  return request({
    url: '/books',
    method: 'get',
    params
  })
}

// 根据ID获取书籍详情
export function getBookById(id) {
  return request({
    url: `/books/${id}`,
    method: 'get'
  })
}

// 发布新书籍
export function publishBook(data) {
  return request({
    url: '/books',
    method: 'post',
    data
  })
}

// 更新书籍信息
export function updateBook(id, data) {
  return request({
    url: `/books/${id}`,
    method: 'put',
    data
  })
}

// 下架书籍
export function offShelfBook(id) {
  return request({
    url: `/books/${id}/off-shelf`,
    method: 'put'
  })
}

// ✅ 删除书籍（已添加，与BookDetail.vue中的调用完全匹配）
export function deleteBook(id) {
  return request({
    url: `/books/${id}`,
    method: 'delete'
  })
}