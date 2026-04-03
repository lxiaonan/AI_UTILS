export const MARKETING_CATEGORIES = ['护肤', '美妆', '食品', '数码', '家居', '服饰', '其他'];
export const MARKETING_PLATFORMS = [
  { value: 'xiaohongshu', label: '小红书' },
  { value: 'douyin', label: '抖音' },
  { value: 'bilibili', label: 'B站' }
];
export const MARKETING_STYLES = ['真诚分享', '种草推荐', '专业测评', '高级品牌感', '强促销', '轻松活泼'];
export const MARKETING_LENGTHS = [
  { value: 'short', label: '简短' },
  { value: 'standard', label: '标准' },
  { value: 'long', label: '详细' }
];
export const MARKETING_CTAS = ['引导购买', '引导评论', '引导收藏', '引导私信'];
export const BRAND_TONES = ['高级', '年轻', '专业', '温暖', '极简', '科技感'];
export const SLOGAN_TYPES = [
  { value: 'slogan', label: 'slogan' },
  { value: 'adcopy', label: '广告语' },
  { value: 'ecommerceTitle', label: '电商主图文案' },
  { value: 'shortVideoTitle', label: '短视频标题' },
  { value: 'socialTitle', label: '社媒标题' }
];

export const MARKETING_EXAMPLE = {
  productName: '轻氧保湿喷雾',
  category: '护肤',
  sellingPointsText: '补水快\n不黏腻\n敏感肌可用\n适合通勤补妆前后使用',
  targetAudience: '上班族女性',
  platforms: ['xiaohongshu', 'douyin', 'bilibili'],
  style: '真诚分享',
  length: 'standard',
  cta: '引导购买'
};

export const SLOGAN_EXAMPLE = {
  productName: '轻氧保湿喷雾',
  category: '护肤',
  sellingPointsText: '补水快\n不黏腻\n适合通勤',
  targetAudience: '上班族女性',
  brandTone: '年轻',
  generateTypes: ['slogan', 'adcopy', 'ecommerceTitle', 'shortVideoTitle', 'socialTitle']
};

export const HISTORY_TYPE_OPTIONS = [
  { value: '', label: '全部' },
  { value: 'marketing_copy', label: '商品文案' },
  { value: 'slogan', label: '广告语' }
];

export function splitLines(value) {
  return String(value || '')
    .split(/\r?\n/)
    .map((item) => item.trim())
    .filter(Boolean);
}

export function safeParseJson(value, fallback = null) {
  try {
    return JSON.parse(value);
  } catch (error) {
    return fallback;
  }
}

export function labelForOption(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function labelsForValues(options, values = []) {
  return values.map((value) => labelForOption(options, value));
}

export async function copyText(text) {
  await navigator.clipboard.writeText(String(text || ''));
}

export function formatListBlock(title, items = []) {
  const normalized = Array.isArray(items) ? items.filter(Boolean) : [];
  if (!normalized.length) {
    return `${title}\n暂无内容`;
  }
  return `${title}\n${normalized.map((item, index) => `${index + 1}. ${item}`).join('\n')}`;
}

export function formatParagraphBlock(title, content) {
  return `${title}\n${content || '暂无内容'}`;
}
