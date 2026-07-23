import request from '@/utils/request'

/**
 * 获取所有分类（平铺结构）
 */
export function getCategories() {
  return request({
    url: '/categories',
    method: 'get'
  })
}

/**
 * 获取一级分类
 */
export function getParentCategories() {
  return request({
    url: '/categories/parents',
    method: 'get'
  })
}

/**
 * 根据父分类ID获取二级分类
 */
export function getChildCategories(parentId) {
  return request({
    url: '/categories/children',
    method: 'get',
    params: { parentId }
  })
}

/**
 * 获取级联分类树（用于级联选择器）
 * 返回格式符合 Element Plus Cascader
 */
export function getCategoryTree() {
  return request({
    url: '/categories/tree',
    method: 'get'
  })
}
