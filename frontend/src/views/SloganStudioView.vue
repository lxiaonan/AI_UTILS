<template>
  <AppShellLayout>
    <div class="max-w-[1440px] mx-auto px-4 pb-12 w-full">
      <section class="studio-hero rounded-[2rem] p-6 md:p-8 lg:p-10 mb-8 overflow-hidden">
        <div class="grid gap-8 lg:grid-cols-[1.08fr_0.92fr] items-start">
          <div>
            <div class="studio-kicker">Slogan Lab</div>
            <h1 class="mt-5 text-4xl md:text-5xl font-extrabold gradient-text tracking-tight leading-tight">
              广告语生成
            </h1>
            <p class="mt-4 max-w-3xl text-base md:text-lg text-slate-300 leading-8">
              适合快速产出 slogan、广告语、电商主图文案和社媒标题。输入比商品文案更轻，适合高频反复试。
            </p>

            <div class="mt-6 flex flex-wrap gap-3">
              <span class="studio-chip">高频使用</span>
              <span class="studio-chip">支持多类型同时生成</span>
              <span class="studio-chip">整页复制</span>
              <span class="studio-chip">TXT 导出</span>
            </div>
          </div>

          <div class="grid gap-3 sm:grid-cols-3">
            <div class="studio-metric">
              <div class="studio-metric-label">生成类型</div>
              <div class="studio-metric-value">{{ form.generateTypes.length }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">当前调性</div>
              <div class="studio-metric-value">{{ form.brandTone }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">核心卖点</div>
              <div class="studio-metric-value">{{ sellingPointCount }}</div>
            </div>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
        <section class="xl:col-span-5 glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
          <div class="flex items-start justify-between gap-4 flex-wrap">
            <div>
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Prompt Input</div>
              <h2 class="mt-2 text-2xl font-bold text-slate-100">输入信息</h2>
            </div>
            <div class="flex gap-3 flex-wrap">
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                @click="loadExample"
              >
                加载示例
              </button>
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                @click="resetForm"
              >
                清空
              </button>
            </div>
          </div>

          <div class="mt-6 space-y-5">
            <div>
              <label class="block text-sm font-bold text-slate-300 mb-3">产品名称</label>
              <input v-model.trim="form.productName" type="text" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border" placeholder="例如：轻氧保湿喷雾">
            </div>

            <div class="grid sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">产品类目</label>
                <select v-model="form.category" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
                  <option v-for="item in MARKETING_CATEGORIES" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">目标人群</label>
                <input v-model.trim="form.targetAudience" type="text" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border" placeholder="例如：上班族女性">
              </div>
            </div>

            <div>
              <label class="block text-sm font-bold text-slate-300 mb-3">核心卖点</label>
              <textarea
                v-model.trim="form.sellingPointsText"
                rows="5"
                class="w-full bg-slate-950/55 border border-slate-700 rounded-[1.4rem] p-4 text-slate-100 leading-7 resize-none outline-none neon-border"
                placeholder="每行一条核心卖点"
              />
            </div>

            <div>
              <label class="block text-sm font-bold text-slate-300 mb-3">品牌调性</label>
              <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
                <button
                  v-for="item in BRAND_TONES"
                  :key="item"
                  type="button"
                  class="voice-chip justify-center"
                  :class="{ active: form.brandTone === item }"
                  @click="form.brandTone = item"
                >
                  {{ item }}
                </button>
              </div>
            </div>

            <div>
              <label class="block text-sm font-bold text-slate-300 mb-3">生成类型</label>
              <div class="grid sm:grid-cols-2 gap-3">
                <button
                  v-for="item in SLOGAN_TYPES"
                  :key="item.value"
                  type="button"
                  class="voice-chip justify-center"
                  :class="{ active: form.generateTypes.includes(item.value) }"
                  @click="toggleGenerateType(item.value)"
                >
                  {{ item.label }}
                </button>
              </div>
            </div>

            <div v-if="errorMessage" class="rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">
              {{ errorMessage }}
            </div>

            <div v-if="copyMessage" class="rounded-2xl border border-emerald-800 bg-emerald-900/25 p-4 text-sm text-emerald-200">
              {{ copyMessage }}
            </div>

            <button
              type="button"
              class="w-full bg-gradient-to-r from-sky-600 to-indigo-600 hover:from-sky-500 hover:to-indigo-500 text-white font-bold py-4 px-6 rounded-2xl transition-all btn-glow flex justify-center items-center text-lg"
              :class="{ 'opacity-60 cursor-not-allowed': !canGenerate }"
              :disabled="!canGenerate"
              @click="generateSlogan"
            >
              {{ loading ? '正在生成...' : '生成广告语' }}
            </button>
          </div>
        </section>

        <section class="xl:col-span-7 glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
          <div class="flex items-start justify-between gap-4 flex-wrap">
            <div>
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Result Grid</div>
              <h2 class="mt-2 text-2xl font-bold text-slate-100">生成结果</h2>
            </div>

            <div class="flex gap-3 flex-wrap" v-if="result">
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition"
                @click="copyAll"
              >
                复制全部
              </button>
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-emerald-900/20 text-emerald-200 border border-emerald-500/30 hover:bg-emerald-900/35 transition"
                @click="saveAsTemplate"
              >
                保存为模板
              </button>
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                @click="exportTxt"
              >
                导出 TXT
              </button>
            </div>
          </div>

          <div class="mt-6 studio-stage rounded-[1.7rem] p-5 md:p-6 min-h-[640px]">
            <div v-if="loading" class="h-full flex flex-col items-center justify-center text-center">
              <div class="w-16 h-16 rounded-full border border-sky-500/30 bg-sky-500/10 flex items-center justify-center animate-pulse">
                <div class="w-7 h-7 rounded-full bg-sky-400/70" />
              </div>
              <div class="mt-5 text-lg font-bold text-slate-100">正在生成广告语</div>
              <div class="mt-2 text-sm text-slate-400">会根据产品卖点和品牌调性输出不同类型文案。</div>
            </div>

            <div v-else-if="!result" class="h-full flex items-center justify-center">
              <div class="studio-empty max-w-xl text-center">
                <div class="text-xs uppercase tracking-[0.24em] text-slate-500">Ready To Generate</div>
                <div class="mt-4 text-2xl font-bold text-slate-100">高频轻量文案会显示在这里</div>
                <div class="mt-3 text-sm leading-7 text-slate-400">
                  适合先快速试 slogan 和标题，再回到商品文案页扩成整套平台内容。
                </div>
              </div>
            </div>

            <div v-else class="grid gap-4 md:grid-cols-2">
              <div
                v-for="block in resultBlocks"
                :key="block.title"
                class="rounded-[1.35rem] border border-slate-700/70 bg-slate-950/40 p-4"
              >
                <div class="flex items-start justify-between gap-3 flex-wrap">
                  <div class="text-lg font-bold text-slate-100">{{ block.title }}</div>
                  <button
                    type="button"
                    class="px-3 py-2 rounded-xl bg-slate-800/80 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                    @click="copyBlock(block)"
                  >
                    复制该块
                  </button>
                </div>

                <div class="mt-4 space-y-3">
                  <div
                    v-for="(item, index) in block.items"
                    :key="`${block.title}-${index}`"
                    class="rounded-2xl border border-slate-700/60 bg-slate-900/55 px-4 py-3 text-sm leading-7 text-slate-200"
                  >
                    {{ item }}
                  </div>
                  <div v-if="!block.items.length" class="rounded-2xl border border-slate-700/60 bg-slate-900/55 px-4 py-3 text-sm leading-7 text-slate-500">
                    当前未生成这一类内容
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import AppShellLayout from '@/components/AppShellLayout.vue';
import { api } from '@/lib/api';
import {
  BRAND_TONES,
  MARKETING_CATEGORIES,
  SLOGAN_EXAMPLE,
  SLOGAN_TYPES,
  copyText,
  formatListBlock,
  splitLines
} from '@/lib/marketing';
import { consumePendingTemplate, upsertCustomTemplate } from '@/lib/template-center';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const form = reactive({
  productName: '',
  category: '护肤',
  sellingPointsText: '',
  targetAudience: '',
  brandTone: '年轻',
  generateTypes: ['slogan', 'adcopy', 'ecommerceTitle', 'shortVideoTitle', 'socialTitle']
});

const loading = ref(false);
const errorMessage = ref('');
const copyMessage = ref('');
const result = ref(null);

const sellingPointCount = computed(() => splitLines(form.sellingPointsText).length);
const canGenerate = computed(() => !loading.value && Boolean(form.productName.trim()) && sellingPointCount.value > 0 && form.generateTypes.length > 0);
const resultBlocks = computed(() => {
  if (!result.value) {
    return [];
  }
  return [
    { title: 'slogan', items: result.value.slogan || [] },
    { title: '广告语', items: result.value.adcopy || [] },
    { title: '电商主图文案', items: result.value.ecommerceTitle || [] },
    { title: '短视频标题', items: result.value.shortVideoTitle || [] },
    { title: '社媒标题', items: result.value.socialTitle || [] }
  ];
});

function loadExample() {
  Object.assign(form, { ...SLOGAN_EXAMPLE });
}

function resetForm() {
  Object.assign(form, {
    productName: '',
    category: '护肤',
    sellingPointsText: '',
    targetAudience: '',
    brandTone: '年轻',
    generateTypes: ['slogan', 'adcopy', 'ecommerceTitle', 'shortVideoTitle', 'socialTitle']
  });
  result.value = null;
  errorMessage.value = '';
  copyMessage.value = '';
}

function toggleGenerateType(value) {
  if (form.generateTypes.includes(value)) {
    form.generateTypes = form.generateTypes.filter((item) => item !== value);
    return;
  }
  form.generateTypes = [...form.generateTypes, value];
}

async function generateSlogan() {
  if (!canGenerate.value) {
    errorMessage.value = '请至少填写产品名称、卖点，并选择一种生成类型';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  copyMessage.value = '';

  try {
    const payload = {
      productName: form.productName.trim(),
      category: form.category,
      sellingPoints: splitLines(form.sellingPointsText),
      targetAudience: form.targetAudience.trim(),
      brandTone: form.brandTone,
      generateTypes: form.generateTypes
    };

    const { response, data } = await api.generateSlogan(payload);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    if (!response.ok) {
      throw new Error(data?.message || '广告语生成失败');
    }

    result.value = data;
    await auth.fetchCurrentUser();
  } catch (error) {
    errorMessage.value = error.message || '广告语生成失败';
  } finally {
    loading.value = false;
  }
}

function buildAllText() {
  return resultBlocks.value
    .map((block) => formatListBlock(block.title, block.items))
    .join('\n\n');
}

async function copyAll() {
  try {
    await copyText(buildAllText());
    copyMessage.value = '全部结果已复制';
  } catch (error) {
    errorMessage.value = '复制失败，请检查浏览器剪贴板权限';
  }
}

async function copyBlock(block) {
  try {
    await copyText(formatListBlock(block.title, block.items));
    copyMessage.value = `${block.title} 已复制`;
  } catch (error) {
    errorMessage.value = '复制失败，请检查浏览器剪贴板权限';
  }
}

function exportTxt() {
  if (!result.value) {
    return;
  }
  const blob = new Blob([buildAllText()], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${form.productName || 'marketing-copy'}-${Date.now()}.txt`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function saveAsTemplate() {
  const template = upsertCustomTemplate({
    module: 'slogan',
    title: `${form.productName.trim() || 'Untitled Product'} Slogan Template`,
    description: 'Saved from slogan workspace',
    tags: ['slogan', form.category, form.brandTone].filter(Boolean),
    fields: {
      productName: form.productName.trim(),
      category: form.category,
      sellingPoints: splitLines(form.sellingPointsText),
      targetAudience: form.targetAudience.trim(),
      brandTone: form.brandTone,
      generateTypes: [...form.generateTypes]
    }
  });
  copyMessage.value = `Template saved: ${template.title}`;
  errorMessage.value = '';
}

function applyPendingSloganTemplate() {
  const pending = consumePendingTemplate('slogan');
  if (!pending?.fields) {
    return;
  }

  const fields = pending.fields;
  if (typeof fields.productName === 'string') form.productName = fields.productName;
  if (typeof fields.category === 'string' && fields.category) form.category = fields.category;
  if (Array.isArray(fields.sellingPoints)) form.sellingPointsText = fields.sellingPoints.join('\n');
  if (typeof fields.targetAudience === 'string') form.targetAudience = fields.targetAudience;
  if (typeof fields.brandTone === 'string' && fields.brandTone) form.brandTone = fields.brandTone;
  if (Array.isArray(fields.generateTypes) && fields.generateTypes.length) form.generateTypes = [...fields.generateTypes];
}

onMounted(async () => {
  applyPendingSloganTemplate();
  await auth.ensureAuthenticated(router, route.fullPath);
});
</script>
