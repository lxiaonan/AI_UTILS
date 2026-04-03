<template>
  <AppShellLayout>
    <div class="max-w-[1440px] mx-auto px-4 pb-12 w-full">
      <section class="studio-hero rounded-[2rem] p-6 md:p-8 lg:p-10 mb-8 overflow-hidden">
        <div class="grid gap-8 lg:grid-cols-[1.15fr_0.85fr] items-start">
          <div>
            <div class="studio-kicker">Visual Studio</div>
            <h1 class="mt-5 text-4xl md:text-5xl font-extrabold gradient-text tracking-tight leading-tight">
              图像创作中心
            </h1>
            <p class="mt-4 max-w-3xl text-base md:text-lg text-slate-300 leading-8">
              支持文本生图、图生图和多参考图混合生成。保留你原本页面的创作感，同时把表单、结果区和历史区的层级重新收紧。
            </p>
            <div class="mt-6 flex flex-wrap gap-3">
              <span class="studio-chip">文本生图</span>
              <span class="studio-chip">多参考图融合</span>
              <span class="studio-chip">结果历史回看</span>
              <span class="studio-chip">一键查看与下载</span>
            </div>
          </div>

          <div class="grid gap-3 sm:grid-cols-3 lg:grid-cols-1 xl:grid-cols-3">
            <div class="studio-metric">
              <div class="studio-metric-label">当前模型</div>
              <div class="studio-metric-value">{{ form.model }}</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">参考图</div>
              <div class="studio-metric-value">{{ readyUploadCount }}/3</div>
            </div>
            <div class="studio-metric">
              <div class="studio-metric-label">历史记录</div>
              <div class="studio-metric-value">{{ history.length }}</div>
            </div>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        <div class="lg:col-span-8 space-y-8">
          <section class="glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
            <div class="flex items-start justify-between gap-4 flex-wrap">
              <div>
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Creative Input</div>
                <h2 class="mt-2 text-2xl font-bold text-slate-100">提示词与参考图</h2>
                <p class="mt-2 text-sm text-slate-400">
                  先写清楚主体、风格和光影，再补参考图，生成结果会明显稳定一些。
                </p>
              </div>
              <div class="w-full sm:w-72">
                <label class="block text-xs uppercase tracking-[0.22em] text-slate-500 mb-2">模型选择</label>
                <select
                  v-model="form.model"
                  class="w-full bg-slate-900/70 border border-slate-700 text-slate-200 rounded-2xl px-4 py-3 outline-none neon-border"
                >
                  <option value="qwen-image-2.0">Qwen Image 2.0</option>
                  <option value="qwen-image-2.0-pro">Qwen Image 2.0 Pro</option>
                  <option value="qwen-image-max">Qwen Image Max</option>
                </select>
              </div>
            </div>

            <div class="mt-6 grid gap-3 md:grid-cols-3">
              <button
                v-for="example in promptExamples"
                :key="example.title"
                type="button"
                class="studio-tip-card text-left"
                @click="applyPromptExample(example.prompt)"
              >
                <div class="text-sm font-bold text-slate-100">{{ example.title }}</div>
                <div class="mt-2 text-sm text-slate-400 leading-6">{{ example.prompt }}</div>
              </button>
            </div>

            <div class="mt-6">
              <label class="block text-sm font-bold text-slate-300 mb-3">创作提示词</label>
              <textarea
                v-model.trim="form.prompt"
                rows="6"
                class="w-full bg-slate-950/55 border border-slate-700 rounded-[1.6rem] p-5 text-slate-100 text-base leading-7 resize-none outline-none neon-border placeholder-slate-500"
                placeholder="例如：一只机械狐站在霓虹雨夜的街口，电影级逆光，潮湿反光地面，镜头压低，冷暖对比强烈。"
              />
            </div>

            <div class="mt-6 studio-dropzone rounded-[1.75rem] p-5 md:p-6">
              <div class="flex items-start justify-between gap-4 flex-wrap">
                <div>
                  <div class="text-sm font-bold text-slate-100">参考图上传</div>
                  <div class="mt-2 text-sm text-slate-400">
                    最多 3 张。你可以只写提示词，也可以结合参考图做风格约束。
                  </div>
                </div>
                <div class="text-xs text-slate-500 uppercase tracking-[0.2em]">Image To Image</div>
              </div>

              <div class="mt-5 flex flex-col gap-4">
                <label class="studio-upload-bar">
                  <span class="text-sm font-semibold text-slate-100">选择图片</span>
                  <span class="text-xs text-slate-500">JPG / PNG / WEBP</span>
                  <input type="file" accept="image/*" multiple class="hidden" @change="handleFiles">
                </label>

                <div class="flex items-center justify-between gap-3 flex-wrap">
                  <div class="text-sm" :class="uploadError ? 'text-rose-300' : 'text-slate-400'">
                    {{ uploadStatus || '未上传参考图时，将按纯文本模式生成。' }}
                  </div>
                  <button
                    v-if="uploads.length"
                    type="button"
                    class="px-4 py-2 rounded-xl bg-rose-900/30 text-rose-300 border border-rose-800/60 hover:bg-rose-900/45 transition"
                    @click="clearUploads"
                  >
                    清空图片
                  </button>
                </div>

                <div v-if="uploads.length" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
                  <div v-for="item in uploads" :key="item.id" class="studio-thumb rounded-[1.35rem] overflow-hidden">
                    <div class="aspect-[4/3] bg-slate-950/70">
                      <img :src="item.preview" class="w-full h-full object-cover" alt="">
                    </div>
                    <div class="p-4">
                      <div class="flex items-center justify-between gap-3">
                        <div class="text-sm font-semibold text-slate-100 truncate">{{ item.name }}</div>
                        <span
                          class="text-[11px] px-2 py-1 rounded-full border"
                          :class="item.ready ? 'text-emerald-300 border-emerald-500/30 bg-emerald-900/20' : 'text-sky-300 border-sky-500/30 bg-sky-900/20'"
                        >
                          {{ item.ready ? '已就绪' : '处理中' }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="errorMessage && resultMode !== 'error'" class="mt-5 rounded-2xl border border-rose-800 bg-rose-900/25 p-4 text-sm text-rose-200">
              {{ errorMessage }}
            </div>

            <div class="mt-6 flex items-center justify-between gap-4 flex-wrap">
              <div class="text-sm text-slate-400">
                当前将以
                <span class="text-slate-100 font-semibold">{{ readyUploadCount ? `${readyUploadCount} 张参考图 + 提示词` : '纯提示词' }}</span>
                的方式生成。
              </div>
              <button
                class="min-w-[220px] bg-gradient-to-r from-sky-600 to-indigo-600 hover:from-sky-500 hover:to-indigo-500 text-white font-bold py-4 px-6 rounded-2xl transition-all btn-glow flex justify-center items-center text-lg"
                :class="{ 'opacity-60 cursor-not-allowed': !canGenerate }"
                :disabled="!canGenerate"
                @click="generateImage"
              >
                {{ loading ? '正在生成...' : '立即生成图像' }}
              </button>
            </div>
          </section>

          <section class="glass-panel rounded-[2rem] p-6 md:p-8 border border-slate-700/50">
            <div class="flex items-start justify-between gap-4 flex-wrap mb-6">
              <div>
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Generation Output</div>
                <h2 class="mt-2 text-2xl font-bold text-slate-100">结果预览</h2>
                <p class="mt-2 text-sm text-slate-400">生成成功后可以直接放大查看，或下载到本地继续处理。</p>
              </div>
              <div v-if="resultMode === 'images'" class="text-sm text-slate-400">
                共生成 <span class="text-slate-100 font-semibold">{{ resultImages.length }}</span> 张图像
              </div>
            </div>

            <div class="studio-stage rounded-[1.75rem] p-4 md:p-6 min-h-[460px]">
              <div v-if="loading" class="h-full flex flex-col items-center justify-center text-center">
                <div class="w-16 h-16 rounded-full border border-sky-500/30 bg-sky-500/10 flex items-center justify-center animate-pulse">
                  <div class="w-7 h-7 rounded-full bg-sky-400/70" />
                </div>
                <div class="mt-5 text-lg font-bold text-slate-100">正在提交任务并等待图像返回</div>
                <div class="mt-2 text-sm text-slate-400">模型生成时间受图片复杂度和排队状态影响。</div>
              </div>

              <div v-else-if="resultMode === 'images'" class="grid gap-6">
                <div
                  v-for="url in resultImages"
                  :key="url"
                  class="rounded-[1.5rem] border border-slate-700/70 bg-slate-950/45 p-4 md:p-5"
                >
                  <div class="studio-stage-checker rounded-[1.25rem] overflow-hidden border border-slate-700/60">
                    <img
                      :src="url"
                      class="w-full max-h-[720px] object-contain cursor-pointer transition-transform duration-500 hover:scale-[1.01]"
                      alt=""
                      @click="openImage(url)"
                    >
                  </div>
                  <div class="mt-4 flex gap-3 flex-wrap">
                    <button
                      type="button"
                      class="px-4 py-3 rounded-xl bg-sky-900/30 text-sky-300 border border-sky-500/30 hover:bg-sky-900/45 transition"
                      @click="openImage(url)"
                    >
                      查看大图
                    </button>
                    <button
                      type="button"
                      class="px-4 py-3 rounded-xl bg-slate-800/70 text-slate-100 border border-slate-700 hover:bg-slate-700 transition"
                      @click="downloadImage(url)"
                    >
                      下载图片
                    </button>
                  </div>
                </div>
              </div>

              <div v-else-if="resultMode === 'raw'" class="rounded-[1.5rem] border border-yellow-800 bg-yellow-900/20 p-5 text-yellow-200">
                <div class="font-bold">API 已返回结果，但未解析出图片链接</div>
                <pre class="text-xs mt-3 whitespace-pre-wrap overflow-auto">{{ rawResponse }}</pre>
              </div>

              <div v-else-if="resultMode === 'error'" class="rounded-[1.5rem] border border-rose-800 bg-rose-900/25 p-5 text-rose-200">
                {{ errorMessage }}
              </div>

              <div v-else class="h-full flex items-center justify-center">
                <div class="studio-empty max-w-xl text-center">
                  <div class="text-xs uppercase tracking-[0.24em] text-slate-500">Ready To Render</div>
                  <div class="mt-4 text-2xl font-bold text-slate-100">结果会显示在这里</div>
                  <div class="mt-3 text-sm leading-7 text-slate-400">
                    你可以只输入提示词直接生成，也可以上传参考图做风格或主体约束。
                  </div>
                  <div class="mt-6 flex flex-wrap gap-3 justify-center">
                    <span class="studio-chip">电影感光影</span>
                    <span class="studio-chip">产品海报</span>
                    <span class="studio-chip">角色设定图</span>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>

        <div class="lg:col-span-4 space-y-8">
          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50">
            <div class="text-xs uppercase tracking-[0.22em] text-slate-500">Current Brief</div>
            <h2 class="mt-2 text-xl font-bold text-slate-100">本次创作摘要</h2>
            <div class="mt-5 grid gap-4">
              <div class="studio-tip-card">
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">提示词</div>
                <div class="mt-3 text-sm text-slate-300 leading-7">{{ promptDisplay }}</div>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div class="studio-tip-card">
                  <div class="text-xs uppercase tracking-[0.22em] text-slate-500">参考图状态</div>
                  <div class="mt-3 text-lg font-bold text-slate-100">{{ readyUploadCount }}/{{ uploads.length || 0 }}</div>
                  <div class="mt-1 text-sm text-slate-400">{{ pendingUploadCount ? `仍有 ${pendingUploadCount} 张处理中` : '全部已就绪' }}</div>
                </div>
                <div class="studio-tip-card">
                  <div class="text-xs uppercase tracking-[0.22em] text-slate-500">生成模式</div>
                  <div class="mt-3 text-lg font-bold text-slate-100">{{ generationModeLabel }}</div>
                  <div class="mt-1 text-sm text-slate-400">支持文本、图片或混合输入</div>
                </div>
              </div>
            </div>
          </section>

          <section class="glass-panel rounded-[2rem] p-6 border border-slate-700/50 h-[840px] flex flex-col">
            <div class="flex items-start justify-between gap-4 pb-5 border-b border-slate-700/60">
              <div>
                <div class="text-xs uppercase tracking-[0.22em] text-slate-500">History</div>
                <h2 class="mt-2 text-xl font-bold text-slate-100">创作历史</h2>
              </div>
              <div class="text-xs text-slate-500">{{ history.length }} 条</div>
            </div>

            <div class="flex-1 overflow-y-auto pr-1 mt-5 space-y-4">
              <div v-if="historyLoading" class="text-center text-slate-500 mt-12">正在加载历史...</div>
              <div v-else-if="!history.length" class="studio-empty text-center px-5 py-10">
                <div class="text-sm text-slate-300">还没有生成记录</div>
                <div class="mt-2 text-xs text-slate-500">第一次生成后会自动沉淀到这里。</div>
              </div>
              <article
                v-for="item in history"
                :key="item.id"
                class="studio-history-card"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="text-xs text-slate-400">{{ formatHistoryDate(item.createTime) }}</div>
                  <span class="text-[11px] px-2 py-1 rounded-full bg-slate-900/70 border border-slate-700 text-slate-300">
                    {{ item.aiModel }}
                  </span>
                </div>
                <div class="mt-3 text-sm text-slate-200 leading-7 break-words">
                  {{ getPromptSummary(item.userPrompt) }}
                </div>
                <div
                  v-if="firstImage(item.imageUrls)"
                  class="mt-4 rounded-[1.2rem] overflow-hidden border border-slate-700/60 bg-slate-950/60 cursor-pointer"
                  @click="openImage(firstImage(item.imageUrls))"
                >
                  <img :src="firstImage(item.imageUrls)" class="w-full h-52 object-cover" alt="" loading="lazy">
                </div>
              </article>
            </div>
          </section>
        </div>
      </div>
    </div>
  </AppShellLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import AppShellLayout from '@/components/AppShellLayout.vue';
import { api, resolveAssetUrl } from '@/lib/api';
import { consumePendingTemplate } from '@/lib/template-center';
import { useAuthStore } from '@/stores/auth';

const promptExamples = [
  {
    title: '电影海报',
    prompt: '未来都市夜景中的女主角特写，霓虹反射，强对比边缘光，电影海报构图，质感细腻。'
  },
  {
    title: '产品广告',
    prompt: '高端耳机悬浮在深色背景中，金属高光，水滴飞溅，商业广告级布光，细节锐利。'
  },
  {
    title: '角色设定',
    prompt: '赛博忍者角色站姿设定图，正面三分之二视角，服装层次复杂，概念设计稿风格。'
  }
];

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const form = reactive({
  model: 'qwen-image-2.0',
  prompt: ''
});
const uploads = ref([]);
const uploadStatus = ref('');
const uploadError = ref(false);
const uploading = ref(false);
const loading = ref(false);
const errorMessage = ref('');
const resultMode = ref('empty');
const resultImages = ref([]);
const rawResponse = ref('');
const historyLoading = ref(true);
const history = ref([]);

const readyUploadCount = computed(() => uploads.value.filter((item) => item.ready).length);
const pendingUploadCount = computed(() => uploads.value.length - readyUploadCount.value);
const promptDisplay = computed(() => form.prompt.trim() || '暂未填写提示词，当前更适合用参考图驱动生成。');
const canGenerate = computed(() => !loading.value && !uploading.value && (Boolean(form.prompt.trim()) || readyUploadCount.value > 0));
const generationModeLabel = computed(() => {
  if (readyUploadCount.value > 0 && form.prompt.trim()) return '图文混合';
  if (readyUploadCount.value > 0) return '参考图驱动';
  return '纯文本';
});

function applyPromptExample(prompt) {
  form.prompt = prompt;
}

async function handleFiles(event) {
  const files = Array.from(event.target.files || []);
  event.target.value = '';
  if (!files.length) {
    return;
  }
  if (files.length > 3) {
    errorMessage.value = '最多只能上传 3 张参考图。';
    return;
  }

  uploads.value = files.map((file, index) => ({
    id: `${file.name}-${index}-${Date.now()}`,
    name: file.name,
    preview: '',
    ready: false,
    base64: '',
    url: ''
  }));
  uploadStatus.value = '正在解析并上传参考图...';
  uploadError.value = false;
  uploading.value = true;
  errorMessage.value = '';

  try {
    for (let index = 0; index < files.length; index += 1) {
      const file = files[index];
      const preview = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (loadEvent) => resolve(loadEvent.target.result);
        reader.onerror = () => reject(new Error('读取文件失败'));
        reader.readAsDataURL(file);
      });
      uploads.value[index].preview = preview;

      const formData = new FormData();
      formData.append('file', file);
      const { response, data } = await api.uploadImage(formData);
      if (response.status === 401) {
        await auth.ensureAuthenticated(router, route.fullPath);
        return;
      }
      if (!data.success) {
        throw new Error(`图片 ${file.name} 处理失败：${data.message}`);
      }
      uploads.value[index].base64 = data.base64;
      uploads.value[index].url = resolveAssetUrl(data.url);
      uploads.value[index].ready = true;
    }
    uploadStatus.value = `已成功准备 ${uploads.value.length} 张参考图`;
  } catch (error) {
    uploadStatus.value = `上传失败：${error.message}`;
    uploadError.value = true;
    errorMessage.value = error.message;
  } finally {
    uploading.value = false;
  }
}

function clearUploads() {
  uploads.value = [];
  uploadStatus.value = '';
  uploadError.value = false;
}

function applyPendingImageTemplate() {
  const pending = consumePendingTemplate('image');
  if (!pending?.fields) {
    return;
  }

  const fields = pending.fields;
  if (typeof fields.model === 'string' && fields.model) {
    form.model = fields.model;
  }
  if (typeof fields.prompt === 'string') {
    form.prompt = fields.prompt;
  }
}

function parseImageUrls(data) {
  if (data.output && Array.isArray(data.output.results)) {
    return data.output.results.map((item) => item.url).filter(Boolean);
  }
  if (data.output && Array.isArray(data.output.choices) && data.output.choices[0]?.message?.content) {
    return data.output.choices[0].message.content.map((item) => item.image).filter(Boolean);
  }
  return [];
}

async function generateImage() {
  if (!form.prompt && !uploads.value.length) {
    errorMessage.value = '请输入提示词，或至少上传一张参考图。';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  resultMode.value = 'empty';
  resultImages.value = [];
  rawResponse.value = '';

  try {
    const content = uploads.value.map((item) => ({ image: item.base64 }));
    if (form.prompt) {
      content.push({ text: form.prompt });
    }

    const requestBody = {
      model: form.model,
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

    const localUrls = uploads.value.map((item) => item.url).filter(Boolean);
    const { response, data } = await api.generateImage(requestBody, localUrls);
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }

    const imageUrls = parseImageUrls(data);
    if (imageUrls.length) {
      resultImages.value = imageUrls;
      resultMode.value = 'images';
      await loadHistory();
      await auth.fetchCurrentUser();
      return;
    }
    if (data.code && data.message) {
      throw new Error(`任务生成失败：${data.code} ${data.message}`);
    }
    rawResponse.value = JSON.stringify(data, null, 2);
    resultMode.value = 'raw';
  } catch (error) {
    errorMessage.value = error.message;
    resultMode.value = 'error';
  } finally {
    loading.value = false;
  }
}

async function loadHistory() {
  historyLoading.value = true;
  try {
    const { response, data } = await api.getImageHistory();
    if (response.status === 401) {
      await auth.ensureAuthenticated(router, route.fullPath);
      return;
    }
    history.value = Array.isArray(data) ? data : [];
  } catch (error) {
    history.value = [];
  } finally {
    historyLoading.value = false;
  }
}

function formatHistoryDate(value) {
  return value ? new Date(value).toLocaleString([], { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '--';
}

function getPromptSummary(userPrompt) {
  let displayPrompt = '多模态请求';
  try {
    const reqObj = JSON.parse(userPrompt);
    if (reqObj.input?.prompt) {
      displayPrompt = reqObj.input.prompt;
    } else if (reqObj.input?.messages?.[0]?.content) {
      const textItem = reqObj.input.messages[0].content.find((item) => item.text);
      if (textItem?.text) {
        displayPrompt = textItem.text;
      }
    }
  } catch (error) {
    displayPrompt = userPrompt || '多模态请求';
  }
  return displayPrompt;
}

function firstImage(imageUrls) {
  return imageUrls ? resolveAssetUrl(imageUrls.split(',')[0]) : '';
}

function openImage(url) {
  window.open(url, '_blank');
}

async function downloadImage(url) {
  try {
    const response = await fetch(url, { mode: 'cors' });
    const blob = await response.blob();
    const blobUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = `ai_image_${Date.now()}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(blobUrl);
  } catch (error) {
    const link = document.createElement('a');
    link.href = url;
    link.download = `ai_image_${Date.now()}.png`;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}

onMounted(async () => {
  applyPendingImageTemplate();
  const ok = await auth.ensureAuthenticated(router, route.fullPath);
  if (ok) {
    await loadHistory();
  }
});
</script>
