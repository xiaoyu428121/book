import request from '@/utils/request'

export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

export function createBatchOrder(data) {
  return request({
    url: '/orders/batch',
    method: 'post',
    data
  })
}

export function payOrder(id) {
  return request({
    url: `/orders/${id}/pay`,
    method: 'put'
  })
}

export function getMyBuyOrders() {
  return request({
    url: '/orders/buy',
    method: 'get'
  })
}

export function getMySellOrders() {
  return request({
    url: '/orders/sell',
    method: 'get'
  })
}

export function shipOrder(id) {
  return request({
    url: `/orders/${id}/ship`,
    method: 'put'
  })
}

export function completeOrder(id) {
  return request({
    url: `/orders/${id}/complete`,
    method: 'put'
  })
}

export function cancelOrder(id) {
  return request({
    url: `/orders/${id}/cancel`,
    method: 'put'
  })
}