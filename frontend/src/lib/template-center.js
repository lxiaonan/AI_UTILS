const CUSTOM_TEMPLATES_KEY = 'aiset:template-center:custom-templates';
const PENDING_TEMPLATE_KEY = 'aiset:template-center:pending-template';

export const TEMPLATE_MODULES = [
  { value: 'sql', label: 'SQL 优化', route: '/' },
  { value: 'image', label: '图像生成', route: '/image' },
  { value: 'marketing', label: '商品文案', route: '/marketing' },
  { value: 'slogan', label: '广告语', route: '/slogan' },
  { value: 'video', label: '视频生成', route: '/video' },
  { value: 'voice', label: '语音生成', route: '/voice' },
  { value: 'camera', label: '3D 控图', route: '/camera' }
];

const MODULE_DEFAULT_FIELDS = {
  sql: {
    model: 'gpt-5.4',
    dbType: 'mysql',
    sql: ''
  },
  image: {
    model: 'qwen-image-2.0',
    prompt: ''
  },
  marketing: {
    productName: '',
    category: '护肤',
    sellingPoints: [],
    targetAudience: '',
    platforms: ['xiaohongshu', 'douyin', 'bilibili'],
    style: '真诚分享',
    length: 'standard',
    cta: '引导购买'
  },
  slogan: {
    productName: '',
    category: '护肤',
    sellingPoints: [],
    targetAudience: '',
    brandTone: '年轻',
    generateTypes: ['slogan', 'adcopy', 'ecommerceTitle', 'shortVideoTitle', 'socialTitle']
  },
  video: {
    model: 'wan2.6-t2v',
    prompt: '',
    resolution: '720P',
    size: '1280*720',
    duration: 10,
    shotType: 'multi',
    promptExtend: true,
    audio: true
  },
  voice: {
    text: '',
    languageType: 'Chinese',
    voiceCode: 'Cherry'
  },
  camera: {
    model: 'qwen-image-2.0',
    azimuth: 45,
    elevation: 20,
    distance: 0.8
  }
};

export const STARTER_TEMPLATES = [
  {
    id: 'starter-sql-mysql-report',
    title: 'MySQL Slow Query Review',
    module: 'sql',
    description: 'Analyze a reporting query with joins, grouping, and ordering for index and execution-plan issues.',
    tags: ['mysql', 'reporting', 'index'],
    builtIn: true,
    fields: {
      model: 'gpt-5.4',
      dbType: 'mysql',
      sql: 'SELECT u.region, COUNT(o.id) AS order_count, SUM(o.total_amount) AS revenue\nFROM orders o\nJOIN users u ON u.id = o.user_id\nWHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)\nGROUP BY u.region\nORDER BY revenue DESC;'
    }
  },
  {
    id: 'starter-image-product-hero',
    title: 'Product Hero Banner',
    module: 'image',
    description: 'Generate a premium e-commerce hero image with strong studio lighting and clean composition.',
    tags: ['product', 'banner', 'marketing'],
    builtIn: true,
    fields: {
      model: 'qwen-image-2.0',
      prompt: 'A premium skincare bottle on a reflective stone platform, soft mist in the background, high-end studio lighting, elegant beige and warm gold palette, luxury e-commerce hero shot, ultra detailed'
    }
  },
  {
    id: 'starter-marketing-skincare',
    title: '护肤商品文案套版',
    module: 'marketing',
    description: '面向护肤商品的多平台营销文案模板，适合小红书、抖音和 B 站一起生成。',
    tags: ['marketing', 'skincare', 'copy'],
    builtIn: true,
    fields: {
      productName: '轻氧保湿喷雾',
      category: '护肤',
      sellingPoints: ['补水快', '不黏腻', '敏感肌可用', '适合通勤补妆前后使用'],
      targetAudience: '上班族女性',
      platforms: ['xiaohongshu', 'douyin', 'bilibili'],
      style: '真诚分享',
      length: 'standard',
      cta: '引导购买'
    }
  },
  {
    id: 'starter-marketing-digital',
    title: '数码商品种草文案',
    module: 'marketing',
    description: '适合耳机、键盘、充电器等轻数码商品的种草风营销模板。',
    tags: ['marketing', 'digital', 'douyin'],
    builtIn: true,
    fields: {
      productName: '降噪蓝牙耳机',
      category: '数码',
      sellingPoints: ['佩戴轻', '通勤降噪明显', '续航长', '颜值简洁'],
      targetAudience: '通勤上班族',
      platforms: ['xiaohongshu', 'douyin'],
      style: '种草推荐',
      length: 'standard',
      cta: '引导收藏'
    }
  },
  {
    id: 'starter-slogan-skincare',
    title: '护肤广告语生成',
    module: 'slogan',
    description: '适合护肤、彩妆和个护类品牌，快速生成 slogan 与主图文案。',
    tags: ['slogan', 'beauty', 'title'],
    builtIn: true,
    fields: {
      productName: '轻氧保湿喷雾',
      category: '护肤',
      sellingPoints: ['补水快', '不黏腻', '适合通勤'],
      targetAudience: '上班族女性',
      brandTone: '年轻',
      generateTypes: ['slogan', 'adcopy', 'ecommerceTitle', 'shortVideoTitle', 'socialTitle']
    }
  },
  {
    id: 'starter-slogan-tech',
    title: '科技感广告语生成',
    module: 'slogan',
    description: '适合数码与家电品牌，强调科技感、效率和清晰价值表达。',
    tags: ['slogan', 'tech', 'brand'],
    builtIn: true,
    fields: {
      productName: '智能桌面空气净化器',
      category: '家居',
      sellingPoints: ['净化快', '噪音低', '桌面不占地', '适合办公室'],
      targetAudience: '办公室人群',
      brandTone: '科技感',
      generateTypes: ['slogan', 'adcopy', 'shortVideoTitle', 'socialTitle']
    }
  },
  {
    id: 'starter-video-ad-launch',
    title: 'Launch Ad Motion',
    module: 'video',
    description: 'Create a punchy text-to-video prompt suitable for short-form product marketing.',
    tags: ['video', 'ad', 'marketing'],
    builtIn: true,
    fields: {
      model: 'wan2.6-t2v',
      prompt: 'A sleek electric car emerges from darkness into dramatic studio light, close-up details, fast cinematic cuts, floating dust, premium commercial aesthetic, smooth camera motion, bold finish',
      size: '1280*720',
      duration: 10,
      shotType: 'multi',
      promptExtend: true,
      audio: true
    }
  },
  {
    id: 'starter-voice-brand-host',
    title: 'Brand Host Narration',
    module: 'voice',
    description: 'Warm and polished narration template for brand videos and product demos.',
    tags: ['voice', 'brand', 'narration'],
    builtIn: true,
    fields: {
      text: 'Welcome to our new AI workspace. In just a few minutes, you can generate visuals, videos, and voice assets that are ready for production.',
      languageType: 'English',
      voiceCode: 'Vivian'
    }
  },
  {
    id: 'starter-camera-side-angle',
    title: 'Side Angle Product Orbit',
    module: 'camera',
    description: 'Shift the camera to a clear side perspective with slightly elevated framing.',
    tags: ['3d', 'product', 'angle'],
    builtIn: true,
    fields: {
      model: 'qwen-image-2.0',
      azimuth: 110,
      elevation: 18,
      distance: 0.9
    }
  }
];

function canUseStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined';
}

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

function readJson(key, fallback) {
  if (!canUseStorage()) {
    return clone(fallback);
  }

  try {
    const raw = window.localStorage.getItem(key);
    if (!raw) {
      return clone(fallback);
    }
    return JSON.parse(raw);
  } catch (error) {
    return clone(fallback);
  }
}

function writeJson(key, value) {
  if (!canUseStorage()) {
    return;
  }
  window.localStorage.setItem(key, JSON.stringify(value));
}

export function getModuleDefaults(module) {
  return clone(MODULE_DEFAULT_FIELDS[module] || {});
}

export function getModuleRoute(module) {
  return TEMPLATE_MODULES.find((item) => item.value === module)?.route || '/';
}

export function listCustomTemplates() {
  const templates = readJson(CUSTOM_TEMPLATES_KEY, []);
  return Array.isArray(templates) ? templates.map((item) => ({ ...item, builtIn: false })) : [];
}

export function listAllTemplates() {
  return [...STARTER_TEMPLATES.map((item) => clone(item)), ...listCustomTemplates()];
}

export function createTemplateDraft(module = 'image') {
  return {
    id: '',
    title: '',
    module,
    description: '',
    tags: [],
    builtIn: false,
    fields: getModuleDefaults(module)
  };
}

export function upsertCustomTemplate(template) {
  const templates = listCustomTemplates();
  const nextTemplate = {
    ...clone(template),
    id: template.id || `tpl_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    builtIn: false,
    tags: Array.isArray(template.tags) ? template.tags.filter(Boolean) : []
  };
  const index = templates.findIndex((item) => item.id === nextTemplate.id);

  if (index >= 0) {
    templates[index] = nextTemplate;
  } else {
    templates.unshift(nextTemplate);
  }

  writeJson(CUSTOM_TEMPLATES_KEY, templates);
  return nextTemplate;
}

export function deleteCustomTemplate(templateId) {
  const templates = listCustomTemplates().filter((item) => item.id !== templateId);
  writeJson(CUSTOM_TEMPLATES_KEY, templates);
}

export function duplicateTemplate(template) {
  return {
    ...clone(template),
    id: '',
    title: `${template.title} Copy`,
    builtIn: false
  };
}

export function queueTemplateApplication(template) {
  if (!canUseStorage()) {
    return;
  }

  writeJson(PENDING_TEMPLATE_KEY, {
    id: template.id,
    title: template.title,
    module: template.module,
    fields: clone(template.fields),
    queuedAt: Date.now()
  });
}

export function consumePendingTemplate(module) {
  if (!canUseStorage()) {
    return null;
  }

  const payload = readJson(PENDING_TEMPLATE_KEY, null);
  if (!payload || payload.module !== module) {
    return null;
  }

  window.localStorage.removeItem(PENDING_TEMPLATE_KEY);
  return payload;
}
