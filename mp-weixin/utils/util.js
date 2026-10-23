// 金额：分 → 元字符串
function fen2yuan(fen) {
  if (fen === null || fen === undefined) return '0.00'
  return (fen / 100).toFixed(2)
}

const ORDER_STATUS = {
  PENDING_PAY: '待支付',
  PENDING_ACCEPT: '待接单',
  ACCEPTED: '已接单',
  DEPARTED: '已出发',
  ARRIVED: '已到达',
  SERVING: '服务中',
  PENDING_CONFIRM: '待确认',
  SETTLING: '待评价',
  REVIEWED: '已评价',
  FINISHED: '已完成',
  CANCELLED: '已取消',
  REFUNDING: '退款中',
  REFUNDED: '已退款',
  DISPUTED: '争议中'
}

const DAY_STATUS = {
  PENDING: '待服务',
  SERVING: '服务中',
  DONE: '已完成',
  PAUSED: '已暂停',
  CANCELLED: '已取消'
}

const ENTRY_MODE = {
  SMART_LOCK: '智能锁',
  PROPERTY: '物业开门',
  KEY_DEPOSIT: '钥匙寄存',
  AT_HOME: '主人在家'
}

const SPECIES = { CAT: '猫', DOG: '狗', OTHER: '其他' }

function statusText(s) { return ORDER_STATUS[s] || s || '' }
function dayStatusText(s) { return DAY_STATUS[s] || s || '' }
function entryModeText(s) { return ENTRY_MODE[s] || s || '' }
function speciesText(s) { return SPECIES[s] || s || '' }

// 2026-10-23T14:00:00 → 2026-10-23 14:00
function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

// 给订单对象补充展示字段（列表/详情通用）
function decorateOrder(o) {
  return Object.assign({}, o, {
    statusText: statusText(o.status),
    payAmountYuan: fen2yuan(o.payAmount),
    totalAmountYuan: fen2yuan(o.totalAmount),
    addressText: o.address || '',
    petText: (o.petNames || []).join('、'),
    createdAtText: fmtTime(o.createdAt),
    payExpireAtText: fmtTime(o.payExpireAt)
  })
}

module.exports = {
  fen2yuan,
  statusText,
  dayStatusText,
  entryModeText,
  speciesText,
  fmtTime,
  decorateOrder
}
