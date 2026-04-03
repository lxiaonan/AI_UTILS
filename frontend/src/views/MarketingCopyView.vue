<template>
  <AppShellLayout>
    <div class="max-w-[1440px] mx-auto px-4 pb-12 w-full">
      <section class="studio-hero rounded-[2rem] p-6 md:p-8 lg:p-10 mb-8 overflow-hidden">
        <div class="grid gap-8 lg:grid-cols-[1.08fr_0.92fr] items-start">
          <div>
            <div class="studio-kicker">Marketing Copy</div>
            <h1 class="mt-5 text-4xl md:text-5xl font-extrabold gradient-text tracking-tight leading-tight">
              商品营销文案生成
            </h1>
            <p class="mt-4 max-w-3xl text-base md:text-lg text-slate-300 leading-8">
              输入商品信息，结合可选商品图，一次生成适合小红书、抖音和 B 站的营销内容。
              第一版先把最核心的内容闭环做顺：生成、微调、复制和历史沉淀。
            </p>

            <div class="mt-6 flex flex-wrap gap-3">
              <span class="studio-chip">多平台文案</span>
              <span class="studio-chip">示例一键带入</span>
              <span class="studio-chip">风格微调</span>
              <span class="studio-chip">分块复制</span>
            </div>
          </div>

          <div class="grid gap-3 sm:grid-cols-3">
            <div class="studio-metric">
              <div class="studio-metric-label">卖点条数</div>
              <div class="studio-metric-value">{{ sellingPointCount }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">目标平台</div>
              <div class="studio-metric-value">{{ selectedPlatformText }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">当前风格</div>
              <div class="studio-metric-value">{{ form.style }}</div>
            </div>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
        <section class="xl:col-span-5 glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
          <div class="flex items-start justify-between gap-4 flex-wrap">
            <div>
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Creative Brief</div>
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
            <div class="studio-dropzone rounded-[1.6rem] p-5">
              <div class="flex items-start justify-between gap-3 flex-wrap">
                <div>
                  <div class="text-sm font-bold text-slate-100">商品图片</div>
                  <div class="mt-2 text-sm text-slate-400">MVP 阶段图片可选，先作为素材上传和展示，不做复杂识别。</div>
                </div>
                <label class="studio-upload-bar px-4 py-3">
                  <span class="text-sm font-semibold text-slate-100">{{ imageUploading ? '上传中...' : '上传图片' }}</span>
                  <input type="file" accept="image/png,image/jpeg,image/webp" class="hidden" @change="handleImageUpload">
                </label>
              </div>

              <div v-if="form.imagePreview" class="mt-5 rounded-[1.3rem] overflow-hidden border border-slate-700/60 bg-slate-950/60">
                <img :src="form.imagePreview" alt="" class="w-full h-60 object-cover">
              </div>

              <div class="mt-4 text-sm" :class="imageError ? 'text-rose-300' : 'text-slate-400'">
                {{ imageStatus || '支持 jpg/png/webp，建议控制在 5MB 内。' }}
              </div>
            </div>

            <div>
              <label class="block text-sm font-bold text-slate-300 mb-3">产品名称</label>
              <input v-model.trim="form.productName" type="text" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border" placeholder="例如：轻氧保湿喷雾">
            </div>

            <div class="grid sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">商品类目</label>
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
              <label class="block text-sm font-bold text-slate-300 mb-3">商品卖点</label>
              <textarea
                v-model.trim="form.sellingPointsText"
                rows="5"
                class="w-full bg-slate-950/55 border border-slate-700 rounded-[1.4rem] p-4 text-slate-100 leading-7 resize-none outline-none neon-border"
                placeholder="每行一条，建议 3-5 条"
              />
            </div>

            <div>
              <label class="block text-sm font-bold text-slate-300 mb-3">内容平台</label>
              <div class="grid sm:grid-cols-3 gap-3">
                <button
                  v-for="item in MARKETING_PLATFORMS"
                  :key="item.value"
                  type="button"
                  class="voice-chip justify-center"
                  :class="{ active: form.platforms.includes(item.value) }"
                  @click="togglePlatform(item.value)"
                >
                  {{ item.label }}
                </button>
              </div>
            </div>

            <div class="grid sm:grid-cols-3 gap-4">
              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">内容风格</label>
                <select v-model="form.style" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
                  <option v-for="item in MARKETING_STYLES" :key="item" :value="item">{{ item }}</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">输出长度</label>
                <select v-model="form.length" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
                  <option v-for="item in MARKETING_LENGTHS" :key="item.value" :value="item.value">{{ item.label }}</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-bold text-slate-300 mb-3">CTA 方向</label>
                <select v-model="form.cta" class="w-full bg-slate-950/55 border border-slate-700 rounded-2xl px-4 py-3 text-slate-100 outline-none neon-border">
                  <option v-for="item in MARKETING_CTAS" :key="item" :value="item">{{ item }}</option>
                </select>
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
              @click="generateCopy()"
            >
              {{ loading ? '正在生成...' : '生成文案' }}
            </button>
          </div>
        </section>

        <section class="xl:col-span-7 glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
          <div class="flex items-start justify-between gap-4 flex-wrap">
            <div>
              <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Generated Result</div>
              <h2 class="mt-2 text-2xl font-bold text-slate-100">结果区</h2>
              <p class="mt-2 text-sm text-slate-400">支持重新生成、风格微调、分块复制和整页复制。</p>
            </div>

            <div class="flex gap-3 flex-wrap" v-if="result">
              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition"
                @click="copyCurrentTab"
              >
                复制当前 Tab
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
                @click="generateCopy()"
              >
                重新生成
              </button>
            </div>
          </div>

          <div class="mt-6 flex flex-wrap gap-3">
            <button
              v-for="item in tabOptions"
              :key="item.value"
              type="button"
              class="voice-chip"
              :class="{ active: activeTab === item.value }"
              @click="activeTab = item.value"
            >
              {{ item.label }}
            </button>
          </div>

          <div v-if="result" class="mt-5 flex flex-wrap gap-3">
            <button
              v-for="item in tweakActions"
              :key="item.label"
              type="button"
              class="px-4 py-2 rounded-xl bg-slate-900/65 text-slate-100 border border-slate-700 hover:bg-slate-800 transition"
              @click="generateCopy(item.style)"
            >
              {{ item.label }}
            </button>
          </div>

          <div class="mt-6 studio-stage rounded-[1.7rem] p-5 md:p-6 min-h-[640px]">
            <div v-if="loading" class="h-full flex flex-col items-center justify-center text-center">
              <div class="w-16 h-16 rounded-full border border-sky-500/30 bg-sky-500/10 flex items-center justify-center animate-pulse">
                <div class="w-7 h-7 rounded-full bg-sky-400/70" />
              </div>
              <div class="mt-5 text-lg font-bold text-slate-100">正在生成营销文案</div>
              <div class="mt-2 text-sm text-slate-400">会结合当前表单信息重新生成结构化内容。</div>
            </div>

            <div v-else-if="!result" class="h-full flex items-center justify-center">
              <div class="studio-empty max-w-xl text-center">
                <div class="text-xs uppercase tracking-[0.24em] text-slate-500">Ready To Generate</div>
                <div class="mt-4 text-2xl font-bold text-slate-100">结果会显示在这里</div>
                <div class="mt-3 text-sm leading-7 text-slate-400">
                  先填写产品信息，点击生成后可在不同平台结果之间切换查看。
                </div>
              </div>
            </div>

            <div v-else class="space-y-4">
              <div
                v-for="block in currentBlocks"
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
                  <template v-if="block.type === 'list'">
                    <div
                      v-for="(item, index) in block.items"
                      :key="`${block.title}-${index}`"
                      class="rounded-2xl border border-slate-700/60 bg-slate-900/55 px-4 py-3 text-sm leading-7 text-slate-200"
                    >
                      {{ item }}
                    </div>
                  </template>

                  <template v-else>
                    <div class="rounded-2xl border border-slate-700/60 bg-slate-900/55 px-4 py-3 text-sm leading-7 text-slate-200 whitespace-pre-wrap">
                      {{ block.content || '暂无内容' }}
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <section class="mt-8 glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
        <div class="flex items-start justify-between gap-4 flex-wrap">
          <div>
            <div class="text-xs uppercase tracking-[0.22em] text-slate-500">海报生成</div>
            <h2 class="mt-2 text-2xl font-bold text-slate-100">生成营销海报图片</h2>
            <p class="mt-2 text-sm text-slate-400">
              把营销信息直接转成可用的海报视觉图。系统会优先复用文案结果里的 AI 生图提示词，也可以结合你上传的商品图一起生成。
            </p>
          </div>
          <div class="flex gap-3 flex-wrap">
            <span class="studio-chip">{{ hasReferenceImage ? '参考图已就绪' : '纯提示词模式' }}</span>
            <span class="studio-chip">{{ visualImages.length ? `已生成 ${visualImages.length} 张图片` : '一键海报流程' }}</span>
          </div>
        </div>

        <div class="mt-6 grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
          <div class="xl:col-span-5 space-y-5">
            <div class="rounded-[1.6rem] border border-slate-700/60 bg-slate-950/40 p-5">
              <div class="flex items-start justify-between gap-3 flex-wrap">
                <div>
                  <div class="text-sm font-bold text-slate-100">海报提示词</div>
                  <div class="mt-2 text-sm text-slate-400">
                    可以直接使用 AI 推荐提示词，也可以按你的品牌视觉方向再细调。
                  </div>
                </div>
                <button
                  type="button"
                  class="px-4 py-2 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition"
                  @click="useRecommendedImagePrompt"
                >
                  使用 AI 提示词
                </button>
              </div>

              <textarea
                v-model.trim="imagePromptDraft"
                rows="7"
                class="mt-5 w-full bg-slate-950/55 border border-slate-700 rounded-[1.4rem] p-4 text-slate-100 leading-7 resize-none outline-none neon-border"
                placeholder="描述海报风格、场景、光线、机位、背景和整体品牌氛围。"
              />

              <div class="mt-4 rounded-2xl border border-slate-700/60 bg-slate-900/45 p-4 text-sm text-slate-300 leading-7">
                <div class="font-semibold text-slate-100">推荐基础提示词</div>
                <div class="mt-2 break-words">{{ recommendedImagePrompt }}</div>
              </div>

              <div class="mt-4 text-sm text-slate-400">
                {{ hasReferenceImage ? '当前上传的商品图会一并作为参考图发送，帮助生成结果更贴近真实包装。' : '如果你希望海报更贴近原商品包装，可以先上传一张商品图。' }}
              </div>
            </div>

            <div v-if="visualErrorMessage" class="rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">
              {{ visualErrorMessage }}
            </div>

            <div v-if="visualMessage" class="rounded-2xl border border-emerald-800 bg-emerald-900/25 p-4 text-sm text-emerald-200">
              {{ visualMessage }}
            </div>

            <div class="flex gap-3 flex-wrap">
              <button
                type="button"
                class="min-w-[220px] bg-gradient-to-r from-sky-600 to-indigo-600 hover:from-sky-500 hover:to-indigo-500 text-white font-bold py-4 px-6 rounded-2xl transition-all btn-glow flex justify-center items-center text-lg"
                :class="{ 'opacity-60 cursor-not-allowed': !visualCanGenerate }"
                :disabled="!visualCanGenerate"
                @click="generateMarketingImage"
              >
                {{ visualLoading ? '正在生成海报...' : '生成海报图片' }}
              </button>

              <button
                type="button"
                class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                @click="openImageStudio"
              >
                打开图片工作台
              </button>
            </div>
          </div>

          <div class="xl:col-span-7">
            <div class="studio-stage rounded-[1.7rem] p-5 md:p-6 min-h-[520px]">
              <div v-if="visualLoading" class="h-full flex flex-col items-center justify-center text-center">
                <div class="w-16 h-16 rounded-full border border-sky-500/30 bg-sky-500/10 flex items-center justify-center animate-pulse">
                  <div class="w-7 h-7 rounded-full bg-sky-400/70" />
                </div>
                <div class="mt-5 text-lg font-bold text-slate-100">正在渲染营销海报</div>
                <div class="mt-2 text-sm text-slate-400">图片服务返回结果后，会第一时间展示在这里。</div>
              </div>

              <div v-else-if="visualResultMode === 'images'" class="grid gap-5">
                <div
                  v-for="url in visualImages"
                  :key="url"
                  class="rounded-[1.45rem] border border-slate-700/70 bg-slate-950/40 p-4"
                >
                  <div class="rounded-[1.2rem] overflow-hidden border border-slate-700/60 bg-slate-950/70">
                    <img :src="url" class="w-full max-h-[720px] object-contain cursor-pointer transition-transform duration-500 hover:scale-[1.01]" alt="" @click="openGeneratedImage(url)">
                  </div>
                  <div class="mt-4 flex gap-3 flex-wrap">
                    <button
                      type="button"
                      class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition"
                      @click="openGeneratedImage(url)"
                    >
                      预览
                    </button>
                    <button
                      type="button"
                      class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                      @click="downloadGeneratedImage(url)"
                    >
                      下载
                    </button>
                  </div>
                </div>
              </div>

              <div v-else-if="visualResultMode === 'raw'" class="rounded-[1.5rem] border border-yellow-800 bg-yellow-900/20 p-5 text-yellow-200">
                <div class="font-bold">图片接口已返回结果，但当前没有解析出可展示的图片地址。</div>
                <pre class="text-xs mt-3 whitespace-pre-wrap overflow-auto">{{ visualRawResponse }}</pre>
              </div>

              <div v-else-if="visualResultMode === 'error'" class="rounded-[1.5rem] border border-rose-800 bg-rose-900/25 p-5 text-rose-200">
                {{ visualErrorMessage }}
              </div>

              <div v-else class="h-full flex items-center justify-center">
                <div class="studio-empty max-w-xl text-center">
                  <div class="text-xs uppercase tracking-[0.24em] text-slate-500">准备开始</div>
                  <div class="mt-4 text-2xl font-bold text-slate-100">海报图片会展示在这里</div>
                  <div class="mt-3 text-sm leading-7 text-slate-400">
                    建议先生成营销文案，系统会自动带出更合适的 AI 海报提示词；如果你已经有明确的视觉方向，也可以手动输入提示词直接开始。
                  </div>
                  <div class="mt-6 flex flex-wrap gap-3 justify-center">
                    <span class="studio-chip">品牌海报</span>
                    <span class="studio-chip">产品主视觉</span>
                    <span class="studio-chip">社媒封面图</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import AppShellLayout from '@/components/AppShellLayout.vue';
import { api, resolveAssetUrl } from '@/lib/api';
import {
  MARKETING_CATEGORIES,
  MARKETING_CTAS,
  MARKETING_EXAMPLE,
  MARKETING_LENGTHS,
  MARKETING_PLATFORMS,
  MARKETING_STYLES,
  copyText,
  formatListBlock,
  formatParagraphBlock,
  labelsForValues,
  splitLines
} from '@/lib/marketing';
import { consumePendingTemplate, queueTemplateApplication, upsertCustomTemplate } from '@/lib/template-center';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const form = reactive({
  productName: '',
  category: '护肤',
  sellingPointsText: '',
  targetAudience: '',
  platforms: ['xiaohongshu', 'douyin', 'bilibili'],
  style: '真诚分享',
  length: 'standard',
  cta: '引导购买',
  imageUrl: '',
  imagePreview: ''
});

const tabOptions = [
  { value: 'xiaohongshu', label: '小红书' },
  { value: 'douyin', label: '抖音' },
  { value: 'bilibili', label: 'B站' },
  { value: 'extras', label: '附加结果' }
];

const tweakActions = [
  { label: '改成更种草', style: '种草推荐' },
  { label: '改成更真实', style: '真诚分享' },
  { label: '改成更带货', style: '强促销' },
  { label: '改成更专业', style: '专业测评' }
];

const activeTab = ref('xiaohongshu');
const loading = ref(false);
const imageUploading = ref(false);
const imageStatus = ref('');
const imageError = ref(false);
const errorMessage = ref('');
const copyMessage = ref('');
const result = ref(null);
const imagePromptDraft = ref('');
const autoImagePrompt = ref('');
const visualLoading = ref(false);
const visualErrorMessage = ref('');
const visualMessage = ref('');
const visualResultMode = ref('empty');
const visualImages = ref([]);
const visualRawResponse = ref('');

const sellingPointCount = computed(() => splitLines(form.sellingPointsText).length);
const selectedPlatformText = computed(() => labelsForValues(MARKETING_PLATFORMS, form.platforms).join(' / ') || '未选择');
const canGenerate = computed(() => !loading.value && !imageUploading.value && Boolean(form.productName.trim()) && sellingPointCount.value > 0 && form.platforms.length > 0);
const hasReferenceImage = computed(() => Boolean(form.imagePreview && form.imageUrl));
const recommendedImagePrompt = computed(() => result.value?.extras?.imagePrompt || buildFallbackImagePrompt());
const visualCanGenerate = computed(() => !visualLoading.value && (Boolean(imagePromptDraft.value.trim()) || hasReferenceImage.value));

const currentBlocks = computed(() => {
  if (!result.value) {
    return [];
  }

  const payload = result.value[activeTab.value] || {};
  if (activeTab.value === 'xiaohongshu') {
    return [
      { title: '标题 3 条', type: 'list', items: payload.titles || [] },
      { title: '正文 2 版', type: 'list', items: payload.body || [] },
      { title: '标签', type: 'list', items: payload.hashtags || [] },
      { title: '封面短句', type: 'list', items: payload.coverTexts || [] }
    ];
  }
  if (activeTab.value === 'douyin') {
    return [
      { title: '3 秒钩子', type: 'list', items: payload.hooks || [] },
      { title: '口播文案', type: 'list', items: payload.script || [] },
      { title: '字幕精简版', type: 'list', items: payload.subtitles || [] },
      { title: '结尾引导语', type: 'text', content: payload.cta || '' }
    ];
  }
  if (activeTab.value === 'bilibili') {
    return [
      { title: '标题', type: 'list', items: payload.titles || [] },
      { title: '开场引入', type: 'text', content: payload.intro || '' },
      { title: '测评 / 分享脚本', type: 'text', content: payload.script || '' },
      { title: '结尾总结', type: 'text', content: payload.ending || '' }
    ];
  }
  return [
    { title: '广告语', type: 'list', items: payload.slogans || [] },
    { title: '海报主标题', type: 'list', items: payload.posterTitles || [] },
    { title: '生图提示词', type: 'text', content: payload.imagePrompt || '' },
    { title: '短视频分镜脚本', type: 'list', items: payload.videoScript || [] }
  ];
});

function buildFallbackImagePrompt() {
  const sellingPoints = splitLines(form.sellingPointsText).slice(0, 4).join(', ');
  return [
    form.productName.trim() || '商品',
    form.category,
    sellingPoints,
    form.style,
    '高级电商海报',
    '画面干净利落',
    '商业级布光',
    '高细节质感'
  ].filter(Boolean).join(', ');
}

function syncImagePrompt(nextPrompt) {
  if (!imagePromptDraft.value || imagePromptDraft.value === autoImagePrompt.value) {
    imagePromptDraft.value = nextPrompt;
  }
  autoImagePrompt.value = nextPrompt;
}

function resetVisualState() {
  visualLoading.value = false;
  visualErrorMessage.value = '';
  visualMessage.value = '';
  visualResultMode.value = 'empty';
  visualImages.value = [];
  visualRawResponse.value = '';
}

function useRecommendedImagePrompt() {
  syncImagePrompt(recommendedImagePrompt.value);
  visualMessage.value = '已应用 AI 推荐提示词';
  visualErrorMessage.value = '';
}

function openImageStudio() {
  queueTemplateApplication({
    id: '',
    title: `${form.productName.trim() || '营销海报'} 图片需求`,
    module: 'image',
    fields: {
      model: 'qwen-image-2.0',
      prompt: imagePromptDraft.value.trim() || recommendedImagePrompt.value
    }
  });
  router.push('/image');
}

function parseGeneratedImageUrls(data) {
  if (data?.output && Array.isArray(data.output.results)) {
    return data.output.results.map((item) => resolveAssetUrl(item.url)).filter(Boolean);
  }
  if (data?.output && Array.isArray(data.output.choices) && data.output.choices[0]?.message?.content) {
    return data.output.choices[0].message.content
      .map((item) => resolveAssetUrl(item.image))
      .filter(Boolean);
  }
  return [];
}

function openGeneratedImage(url) {
  window.open(url, '_blank');
}

async function downloadGeneratedImage(url) {
  try {
    const response = await fetch(url, { mode: 'cors' });
    const blob = await response.blob();
    const blobUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = `marketing_visual_${Date.now()}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(blobUrl);
  } catch (error) {
    const link = document.createElement('a');
    link.href = url;
    link.download = `marketing_visual_${Date.now()}.png`;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}

function loadExample() {
  Object.assign(form, {
    ...MARKETING_EXAMPLE,
    imageUrl: form.imageUrl,
    imagePreview: form.imagePreview
  });
  syncImagePrompt(buildFallbackImagePrompt());
  activeTab.value = 'xiaohongshu';
}

function resetForm() {
  Object.assign(form, {
    productName: '',
    category: '护肤',
    sellingPointsText: '',
    targetAudience: '',
    platforms: ['xiaohongshu', 'douyin', 'bilibili'],
    style: '真诚分享',
    length: 'standard',
    cta: '引导购买',
    imageUrl: '',
    imagePreview: ''
  });
  result.value = null;
  errorMessage.value = '';
  imageStatus.value = '';
  imageError.value = false;
  copyMessage.value = '';
  imagePromptDraft.value = '';
  autoImagePrompt.value = '';
  resetVisualState();
}

function togglePlatform(value) {
  if (form.platforms.includes(value)) {
    form.platforms = form.platforms.filter((item) => item !== value);
    return;
  }
  form.platforms = [...form.platforms, value];
}

async function handleImageUpload(event) {
  const file = event.target.files?.[0];
  event.target.value = '';
  if (!file) {
    return;
  }

  imageUploading.value = true;
  imageStatus.value = '正在上传图片...';
  imageError.value = false;
  errorMessage.value = '';

  try {
    const preview = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (loadEvent) => resolve(loadEvent.target.result);
      reader.onerror = () => reject(new Error('图片读取失败'));
      reader.readAsDataURL(file);
    });

    const formData = new FormData();
    formData.append('file', file);
    const { response, data } = await api.uploadImage(formData);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    if (!data?.success) {
      throw new Error(data?.message || '图片上传失败');
    }

    form.imagePreview = preview;
    form.imageUrl = resolveAssetUrl(data.url);
    imageStatus.value = '图片上传完成，可直接用于本次生成';
  } catch (error) {
    imageStatus.value = error.message || '图片上传失败';
    imageError.value = true;
    errorMessage.value = imageStatus.value;
  } finally {
    imageUploading.value = false;
  }
}

async function generateCopy(styleOverride = '') {
  if (!canGenerate.value) {
    errorMessage.value = '请至少填写产品名称、卖点，并选择一个平台';
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
      platforms: form.platforms,
      style: styleOverride || form.style,
      length: form.length,
      cta: form.cta,
      imageUrl: form.imageUrl
    };

    const { response, data } = await api.generateMarketingCopy(payload);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    if (!response.ok) {
      throw new Error(data?.message || '营销文案生成失败');
    }

    result.value = data;
    syncImagePrompt(data?.extras?.imagePrompt || buildFallbackImagePrompt());
    if (styleOverride) {
      form.style = styleOverride;
    }
    activeTab.value = form.platforms[0] || 'xiaohongshu';
    await auth.fetchCurrentUser();
  } catch (error) {
    errorMessage.value = error.message || '营销文案生成失败';
  } finally {
    loading.value = false;
  }
}

async function generateMarketingImage() {
  if (!visualCanGenerate.value) {
    visualErrorMessage.value = '请先填写海报提示词，或先上传一张商品图片。';
    return;
  }

  visualLoading.value = true;
  visualErrorMessage.value = '';
  visualMessage.value = '';
  visualResultMode.value = 'empty';
  visualImages.value = [];
  visualRawResponse.value = '';

  try {
    const content = [];
    if (form.imagePreview) {
      content.push({ image: form.imagePreview });
    }
    if (imagePromptDraft.value.trim()) {
      content.push({ text: imagePromptDraft.value.trim() });
    }

    const payload = {
      model: 'qwen-image-2.0',
      input: {
        messages: [
          { role: 'user', content }
        ]
      },
      parameters: {
        n: 1,
        negative_prompt: '',
        watermark: false,
        prompt_extend: true
      }
    };

    const localUrls = form.imageUrl ? [form.imageUrl] : [];
    const { response, data } = await api.generateImage(payload, localUrls);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }

    const urls = parseGeneratedImageUrls(data);
    if (urls.length) {
      visualImages.value = urls;
      visualResultMode.value = 'images';
      visualMessage.value = '营销海报已生成。';
      await auth.fetchCurrentUser();
      return;
    }

    if (data?.code && data?.message) {
      throw new Error(`${data.code}: ${data.message}`);
    }

    visualRawResponse.value = JSON.stringify(data, null, 2);
    visualResultMode.value = 'raw';
  } catch (error) {
    visualErrorMessage.value = error.message || '图片生成失败。';
    visualResultMode.value = 'error';
  } finally {
    visualLoading.value = false;
  }
}

async function copyCurrentTab() {
  try {
    const content = currentBlocks.value
      .map((block) => (block.type === 'list'
        ? formatListBlock(block.title, block.items)
        : formatParagraphBlock(block.title, block.content)))
      .join('\n\n');
    await copyText(content);
    copyMessage.value = '当前 Tab 内容已复制';
  } catch (error) {
    errorMessage.value = '复制失败，请检查浏览器剪贴板权限';
  }
}

async function copyBlock(block) {
  try {
    const content = block.type === 'list'
      ? formatListBlock(block.title, block.items)
      : formatParagraphBlock(block.title, block.content);
    await copyText(content);
    copyMessage.value = `${block.title} 已复制`;
  } catch (error) {
    errorMessage.value = '复制失败，请检查浏览器剪贴板权限';
  }
}

function saveAsTemplate() {
  const template = upsertCustomTemplate({
    module: 'marketing',
    title: `${form.productName.trim() || 'Untitled Product'} Template`,
    description: 'Saved from marketing copy workspace',
    tags: ['marketing', form.category, form.style].filter(Boolean),
    fields: {
      productName: form.productName.trim(),
      category: form.category,
      sellingPoints: splitLines(form.sellingPointsText),
      targetAudience: form.targetAudience.trim(),
      platforms: [...form.platforms],
      style: form.style,
      length: form.length,
      cta: form.cta,
      imageUrl: form.imageUrl
    }
  });
  copyMessage.value = `Template saved: ${template.title}`;
  errorMessage.value = '';
}

function applyPendingMarketingTemplate() {
  const pending = consumePendingTemplate('marketing');
  if (!pending?.fields) {
    return;
  }

  const fields = pending.fields;
  if (typeof fields.productName === 'string') form.productName = fields.productName;
  if (typeof fields.category === 'string' && fields.category) form.category = fields.category;
  if (Array.isArray(fields.sellingPoints)) form.sellingPointsText = fields.sellingPoints.join('\n');
  if (typeof fields.targetAudience === 'string') form.targetAudience = fields.targetAudience;
  if (Array.isArray(fields.platforms) && fields.platforms.length) form.platforms = [...fields.platforms];
  if (typeof fields.style === 'string' && fields.style) form.style = fields.style;
  if (typeof fields.length === 'string' && fields.length) form.length = fields.length;
  if (typeof fields.cta === 'string' && fields.cta) form.cta = fields.cta;
  if (typeof fields.imageUrl === 'string' && fields.imageUrl) {
    form.imageUrl = fields.imageUrl;
    form.imagePreview = fields.imageUrl;
    imageStatus.value = 'Image restored from template or history';
    imageError.value = false;
  }
}

onMounted(async () => {
  applyPendingMarketingTemplate();
  if (!imagePromptDraft.value) {
    syncImagePrompt(buildFallbackImagePrompt());
  }
  await auth.ensureAuthenticated(router, route.fullPath);
});
</script>
