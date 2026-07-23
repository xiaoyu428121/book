import request from '@/utils/request'

// 获取暂存架列表
export function getCartList() {
  return request({
    url: '/cart/list',
    method: 'get'
  })
}

// 添加到暂存架
export function addToCart(data) {
  return request({
    url: '/cart/add',
    method: 'post',
    data
  })
}

// 从暂存架移除
export function removeFromCart(id) {
  return request({
    url: `/cart/${id}`,
    method: 'delete'
  })
}